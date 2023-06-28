package io.redlink.more.studymanager.component.observation.lime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.more.studymanager.component.observation.lime.model.*;
import io.redlink.more.studymanager.core.factory.ComponentFactoryProperties;
import io.redlink.more.studymanager.core.ui.OptionValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

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

    protected void activateSurvey(String surveyId) {
        try{
            String sessionKey = getSessionKey();
            HttpRequest request = createHttpRequest(
                    parseRequest("activate_survey",
                            List.of(sessionKey, surveyId))
            );
            client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            releaseSessionKey(sessionKey);
        }catch (IOException | InterruptedException e) {
            LOGGER.error("Error activating survey {}", surveyId);
            throw new RuntimeException(e);
        }
    }

    protected void setSurveyEndUrl(String surveyId, Long studyId, int observationId) {
        try{
            String sessionKey = getSessionKey();
            String lang = getLanguage(surveyId, sessionKey);
            HttpRequest request = createHttpRequest(
                    parseRequest("set_language_properties",
                            List.of(sessionKey, surveyId,
                                    Map.of(
                                            "surveyls_url",
                                            properties.get("endUrl") + getSurveyEndUrlQuery(studyId, observationId)),
                                    lang
                            ))
            );
            String b = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            releaseSessionKey(sessionKey);
            LOGGER.info(b);
        }catch (IOException | InterruptedException e) {
            LOGGER.error("Error setting ref url for survey {}", surveyId, e);
            throw new RuntimeException(e);
        }
    }

    private String getSurveyEndUrlQuery(Long studyId, int observationId) {
        return String.format("?savedid={SAVEDID}&surveyid={SID}&token={TOKEN}&studyId=%s&observationId=%s",
                studyId, observationId);
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

    public String getLanguage(String surveyId, String sessionKey) {
        try{
            HttpRequest request = createHttpRequest(
                    parseRequest("get_language_properties",
                            List.of(sessionKey, surveyId, List.of("surveyls_language")))
            );
            client.send(request, HttpResponse.BodyHandlers.ofString()).body();

            JsonNode node = mapper.readTree(client.send(request, HttpResponse.BodyHandlers.ofString()).body());

            return node.get("result").get("surveyls_language").asText();

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

            releaseSessionKey(sessionKey);

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

    private void releaseSessionKey(String sessionKey){
        try {
            HttpRequest request = createHttpRequest(
                    parseRequest("release_session_key",
                            List.of(sessionKey)));
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch(IOException | InterruptedException e){
            LOGGER.error("Error releasing session key for Limesurvey remote control");
            throw new RuntimeException(e);
        }
    }

    protected String getRemoteUrl(){
        return properties.get("remoteUrl").toString();
    }
    public String getSurveyUrl(){
        return properties.get("surveyUrl").toString();
    }

    public JsonNode listSurveysByUser (String username, String filter, Integer start, Integer size){
        try {
            String sessionKey = getSessionKey();
            HttpRequest listSurveysRequest = createHttpRequest(parseRequest("list_surveys", List.of(sessionKey, username)));
            LimeSurveyListSurveyResponse response =
                    mapper.readValue(client.send(listSurveysRequest, HttpResponse.BodyHandlers.ofString()).body(),
                            LimeSurveyListSurveyResponse.class);
            releaseSessionKey(sessionKey);

            if(response.error() != null) {
                LOGGER.error("Error getting surveys for user: {}", response.error());
                throw new RuntimeException(response.error());
            }

            List<SurveyData> result = response.result().stream()
                    .filter(res -> matchesFilter(filter, res.surveyTitle()))
                    .toList();
            return mapper.convertValue(
                    sublist(result, start, size).stream().map(d -> new OptionValue()
                            .setName(d.surveyTitle()).setValue(d.surveyId())).toList(),
                    JsonNode.class
            );
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Error getting surveys for user", e);
            throw new RuntimeException(e);
        }
    }

    private List<SurveyData> sublist(List <SurveyData> list, Integer start, Integer size){
        if(start != null && start > list.size()-1) {
            return List.of();
        }
        if (start != null && size != null) {
            return list.subList(start, Math.min(list.size(), start + size));
        }
        return list;
    }

    private HttpRequest createHttpRequest (String body){
        return HttpRequest.newBuilder()
                .uri(URI.create(getRemoteUrl()))
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

    private boolean matchesFilter(String filter, String text){
        return Optional.ofNullable(filter)
                .map(f -> text.toLowerCase().contains(f.toLowerCase()))
                .orElse(true);
    }

    public Optional<Map> getAnswer(String token, int surveyId, int savedId) {
        try{
            String sessionKey = getSessionKey();
            String lang = getLanguage(String.valueOf(surveyId), sessionKey);
            HttpRequest request = createHttpRequest(
                    parseRequest("export_responses_by_token",
                            List.of(sessionKey, surveyId, "json", token, lang))
            );
            var responseBody = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            releaseSessionKey(sessionKey);

            JsonNode responseNode = mapper.readTree(responseBody);

            if(!responseNode.get("error").isEmpty()) {
                return Optional.empty();
            }

            if(responseNode.get("result").isTextual()) {
                JsonNode result = mapper.readTree(Base64.getDecoder().decode(responseNode.get("result").asText()));
                Iterator<JsonNode> responses = result.get("responses").elements();
                while(responses.hasNext()) {
                    JsonNode response = responses.next();
                    if(response.get("id").asInt() == savedId) {
                        return Optional.of(mapper.treeToValue(response, Map.class));
                    }
                }
            }
            return Optional.empty();

        }catch (IOException | InterruptedException e) {
            LOGGER.error("Error reading results for {}", surveyId);
            return Optional.empty();
        }
    }
}
