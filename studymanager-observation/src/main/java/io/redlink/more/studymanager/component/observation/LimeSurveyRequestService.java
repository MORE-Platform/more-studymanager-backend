package io.redlink.more.studymanager.component.observation;

import io.redlink.more.studymanager.component.observation.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.more.studymanager.core.factory.ComponentFactoryProperties;

import java.io.IOException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LimeSurveyRequestService {

    private final ComponentFactoryProperties properties;
    private final HttpClient client;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(LimeSurveyRequestService.class);


    protected LimeSurveyRequestService(ComponentFactoryProperties properties){
        this.properties = properties;
        client = HttpClient.newHttpClient();
    }

    protected LimeSurveyRequestService(HttpClient client, ComponentFactoryProperties properties){
        this.properties = properties;
        this.client = client;
    }

    protected List<ParticipantData> activateParticipants(Set<Integer> participantIds, String surveyId){
        String sessionKey = getSessionKey();
        createParticipantTable(surveyId, sessionKey);
        return createParticipants(participantIds, surveyId, sessionKey);
    }

    private void createParticipantTable(String surveyId, String sessionKey){
        try{
            HttpRequest request = createHttpRequest(
                        parseRequest("activate_tokens",
                                List.of(sessionKey, surveyId))
                );
            client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        }catch (IOException | InterruptedException e) {
            LOGGER.error("Error creating participant table for survey {}", surveyId);
            throw new RuntimeException(e);
        }
    }

    /**
     * This method sets both firstname and lastname of lime-participants as the id of more-participants
     */
    private List<ParticipantData> createParticipants(Set<Integer> participantIds, String surveyId, String sessionKey){
        try{
            HttpRequest request = createHttpRequest(
                        parseRequest("add_participants",
                                List.of(sessionKey, surveyId, participantIds.stream().map(i ->
                                                new ParticipantData(i.toString(), i.toString(), null)
                                        ).toList()))
                );
            LOGGER.info("sent {} participants to lime", participantIds.size());
            List<ParticipantData> data = mapper.readValue(client.send(request, HttpResponse.BodyHandlers.ofString()).body(),
                            LimeSurveyParticipantResponse.class)
                    .result();
            LOGGER.info("result: {}", data.stream().map(ParticipantData::toString).collect(Collectors.joining()));
            return data;
        }catch(IOException | InterruptedException e){
            LOGGER.error("Error creating participants for survey {}", surveyId);
            throw new RuntimeException(e);
        }
    }

    private String getSessionKey(){
        try {
            HttpRequest request = createHttpRequest(
                        parseRequest("get_session_key",
                                List.of(properties.get("username"), properties.get("password")))
                );
            return mapper.readValue(
                    client.send(request, HttpResponse.BodyHandlers.ofString()).body(),
                    LimeSurveyObjectResponse.class)
                    .result().toString();
        } catch(IOException | InterruptedException e){
            LOGGER.error("Error getting session key for Limesurvey remote control");
            throw new RuntimeException(e);
        }
    }

    protected String getUrl(){
        return properties.get("url").toString();
    }

    public JsonNode listSurveysByUser (String username, String filter, Integer start, Integer size){
        try {
            HttpRequest listSurveysRequest = createHttpRequest(parseRequest("list_surveys", List.of(getSessionKey(), username)));
            LimeSurveyListSurveyResponse response =
                    mapper.readValue(client.send(listSurveysRequest, HttpResponse.BodyHandlers.ofString()).body(),
                            LimeSurveyListSurveyResponse.class);
            response.result().forEach(entry -> {
                if (!isInFilter(filter, entry.surveyTitle())) { response.result().remove(entry); }
            });
            return mapper.convertValue(handlePagination(response.result(), start, size), JsonNode.class);
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Error getting surveys for user");
            throw new RuntimeException(e);
        }
    }

    private List<SurveyData> handlePagination (List <SurveyData> list, Integer start, Integer
    size){
        if (start != null && size != null) {
            List<SurveyData> sublist = list.subList(start, list.size());
            list = size < sublist.size() ? sublist.subList(0, size) : sublist;
        }
        return list;
    }

    private HttpRequest createHttpRequest (String body){
        return HttpRequest.newBuilder()
                .uri(URI.create(getUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest
                        .BodyPublishers
                        .ofString(body))
                .build();
    }

    protected String parseRequest(String method, List<Object> params) throws JsonProcessingException {
        return mapper.writeValueAsString(
                new LimeSurveyRequest(method, params, 1)
        );
    }

    private boolean isInFilter (String filter, String text){
        if (filter == null)
            return true;
        return text.toLowerCase().contains(filter.toLowerCase());
    }
}