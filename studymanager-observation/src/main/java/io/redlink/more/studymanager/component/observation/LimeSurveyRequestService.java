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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LimeSurveyRequestService {

    private final ComponentFactoryProperties properties;
    private final HttpClient client;
    private final ObjectMapper mapper = new ObjectMapper();

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

    protected void createParticipantTable(String surveyId, String sessionKey){
        try{
            HttpRequest request = createHttpRequest(
                        parseRequest("activate_tokens",
                                List.of(sessionKey, surveyId),
                                1)
                );
            client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        }catch (IOException | InterruptedException ignored){}
    }

    protected List<ParticipantData> createParticipants(Set<Integer> participantIds, String surveyId, String sessionKey){
        try{
            HttpRequest request = createHttpRequest(
                        parseRequest("add_participants",
                                List.of(sessionKey, surveyId, participantIds.stream().map(i ->
                                                new ParticipantData(i.toString(), i.toString(), null)
                                        ).toList()),
                                1)
                );
            return mapper.readValue(client.send(request, HttpResponse.BodyHandlers.ofString()).body(),
                            LimeSurveyParticipantResponse.class)
                    .result();
        }catch(IOException | InterruptedException e){
            return new ArrayList<>();
        }
    }

    protected String getSessionKey(){
        try {
            HttpRequest request = createHttpRequest(
                        parseRequest("get_session_key",
                                List.of(properties.get("username"), properties.get("password")),
                                1)
                );
            return mapper.readValue(
                    client.send(request, HttpResponse.BodyHandlers.ofString()).body(),
                    LimeSurveyObjectResponse.class)
                    .result().toString();
        }catch(IOException | InterruptedException e){
            e.printStackTrace();
            return "";
        }
    }

    protected String getUrl(){
        return properties.get("url").toString();
    }

    public JsonNode listSurveysByUser (String username, String filter, Integer start, Integer size){
        try {
            HttpRequest listSurveysRequest = createHttpRequest(parseRequest("list_surveys", List.of(getSessionKey(), username), 1));
            LimeSurveyListSurveyResponse response =
                    mapper.readValue(client.send(listSurveysRequest, HttpResponse.BodyHandlers.ofString()).body(),
                            LimeSurveyListSurveyResponse.class);
            response.result().forEach(entry -> {
                if (!isInFilter(filter, entry.surveyTitle())) { response.result().remove(entry); }
            });
            return mapper.convertValue(handlePagination(response.result(), start, size), JsonNode.class);
        } catch (IOException | InterruptedException e) {
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

    protected String parseRequest(String method, List<Object> params, Integer id) throws JsonProcessingException {
        return mapper.writeValueAsString(
                new LimeSurveyRequest(method, params, id)
        );
    }

    private boolean isInFilter (String filter, String text){
        if (filter == null)
            return true;
        return text.toLowerCase().contains(filter.toLowerCase());
    }
}
