package io.redlink.more.studymanager.component.observation;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.*;
import io.redlink.more.studymanager.component.observation.model.LimeSurveyRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.more.studymanager.component.observation.model.LimeSurveyKeyResponse;
import io.redlink.more.studymanager.component.observation.model.LimeSurveyRequest;
import io.redlink.more.studymanager.component.observation.model.LimeSurveyResponse;
import io.redlink.more.studymanager.component.observation.model.ParticipantData;
import io.redlink.more.studymanager.core.factory.ComponentFactoryProperties;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LimeSurveyRequestService {

    private final ComponentFactoryProperties properties;
    private final HttpClient client;
    private final Gson gson;
    private final ObjectMapper mapper = new ObjectMapper();
    private final TypeReference<Map<String, String>> mapStringStringRef
            = new TypeReference<>() {
    };
    private final ObjectMapper objectMapper;

    protected LimeSurveyRequestService(ComponentFactoryProperties properties) {
        this.properties = properties;
        client = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
    }

    protected LimeSurveyRequestService(HttpClient client, ComponentFactoryProperties properties){
        this.properties = properties;
        this.client = client;
        objectMapper = new ObjectMapper();
    }

    protected List<String> activateParticipants(Set<Integer> participantIds, String surveyId) {
        String sessionKey = getSessionKey();
        if (createParticipantTable(surveyId, sessionKey))
            return createParticipants(participantIds, surveyId, sessionKey);
        return new ArrayList<>();
        createParticipantTable(surveyId, sessionKey);
        return createParticipants(participantIds, surveyId, sessionKey);
    }

    private boolean createParticipantTable(String surveyId, String sessionKey) {
        HttpRequest request = HttpRequest.newBuilder()
    protected void createParticipantTable(String surveyId, String sessionKey){
        LimeSurveyRequest request = new LimeSurveyRequest(
                "activate_tokens",
                Map.of(
                        "SessionKey", sessionKey,
                        "SurveyId", surveyId
                ),
                1
        );
        try{
            HttpRequest createTableRequest = HttpRequest.newBuilder()
                .uri(URI.create(getUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        objectMapper.writeValueAsString(request)
                ))
                .build();
            client.send(createTableRequest, HttpResponse.BodyHandlers.ofString()).body();
        }catch (IOException | InterruptedException ignored){}
    }

    protected List<String> createParticipants(Set<Integer> participantIds, String surveyId, String sessionKey){
        LimeSurveyRequest request = new LimeSurveyRequest(
                "add_participants",
                Map.of(
                        "SessionKey", sessionKey,
                        "SurveyId", surveyId,
                        "ParticipantData", participantIds.stream().map(i ->
                                new ParticipantData(i.toString(), i.toString())
                        ).toList()),
                1
        );
        try{
            HttpRequest createParticipantsRequest = HttpRequest.newBuilder()
                .uri(URI.create(getUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        objectMapper.writeValueAsString(request)
                )).build();
            return objectMapper.readValue(client.send(createParticipantsRequest, HttpResponse.BodyHandlers.ofString()).body(),
                            LimeSurveyResponse.class)
                    .result().stream().map(
                            entry -> entry.get("firstname") +
                                    "," +
                                    entry.get("token")
                    ).toList();
        } catch (IOException | InterruptedException e) {
            return new ArrayList<>();
        }
    }

    protected String getSessionKey(){
        LimeSurveyRequest request = new LimeSurveyRequest(
                "get_session_key",
                Map.of("username", properties.get("username"),
                        "password", properties.get("password")),
                1
        );
        try {
            HttpRequest sessionKeyRequest = HttpRequest.newBuilder()
                .uri(URI.create(getUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        objectMapper.writeValueAsString(request)
                ))
                .build();
            return objectMapper.readValue(
                    client.send(sessionKeyRequest, HttpResponse.BodyHandlers.ofString()).body(),
                    LimeSurveyKeyResponse.class)
                    .result();
        }catch(IOException | InterruptedException e){
            e.printStackTrace();
            return "";
        }
    }

    protected String getUrl(){
        return properties.get("url").toString();
    }

    private String getSessionKey () {
        HttpRequest sessionKeyRequest = createHttpRequest(
                "{\"method\": \"get_session_key\", \"params\": [\"more-admin\", \"\"], \"id\": 1}");
        try {
            String response = client.send(sessionKeyRequest, HttpResponse.BodyHandlers.ofString()).body();
            return mapper.readValue(response, mapStringStringRef).get("result");
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> transformResultToMap (String content) throws JsonProcessingException {
        TypeReference<Map<String, Object>> typeRef
                = new TypeReference<>() {
        };
        return mapper.readValue(content, typeRef);
    }

    public JsonNode listSurveysByUser (String username, String filter, Integer start, Integer size){
        try {
            LimeSurveyRequest request = new LimeSurveyRequest("list_surveys", List.of(getSessionKey(), username), 1);
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(request);

            HttpRequest listSurveysRequest = createHttpRequest(json);
            String response = client.send(listSurveysRequest, HttpResponse.BodyHandlers.ofString()).body();
            Map<String, Object> responseAsMap = transformResultToMap(response);
            if (responseAsMap.get("error") != null) {
                return mapper.convertValue(responseAsMap, JsonNode.class);
            }
            ArrayNode studies = mapper.convertValue(responseAsMap.get("result"), ArrayNode.class);
            List<Map<String, Object>> transformedResult = new ArrayList<>();
            studies.forEach(entry -> {
                if (isInFilter(filter, entry.get("surveyls_title").asText())) {
                    transformedResult
                            .add(Map.of("surveyId", entry.get("sid").asText(),
                                    "surveyTitle", entry.get("surveyls_title").asText()));
                }
            });
            List<Map<String, Object>> paginatedResult = handlePagination(transformedResult, start, size);
            return mapper.convertValue(paginatedResult, JsonNode.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Map<String, Object>> handlePagination (List < Map < String, Object >> list, Integer start, Integer
    size){
        if (start != null && size != null) {
            List<Map<String, Object>> sublist = list.subList(start, list.size());
            list = size < sublist.size() ? sublist.subList(0, size) : sublist;
        }
        return list;
    }

    private HttpRequest createHttpRequest (String body){
        return HttpRequest.newBuilder()
                .uri(URI.create("https://lime.platform-test.more.redlink.io/admin/remotecontrol"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest
                        .BodyPublishers
                        .ofString(body))
                .build();
    }

    private boolean isInFilter (String filter, String text){
        if (filter == null)
            return true;
        return text.toLowerCase().contains(filter.toLowerCase());
    }
}
