package io.redlink.more.studymanager.component.observation;

import com.google.gson.*;
import io.redlink.more.studymanager.core.factory.ComponentFactoryProperties;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.*;
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

    protected LimeSurveyRequestService(ComponentFactoryProperties properties, HttpClient client) {
        this.properties = properties;
        this.client = client;
        gson = new Gson();
    }

    protected LimeSurveyRequestService(ComponentFactoryProperties properties) {
        this.properties = properties;
        client = HttpClient.newHttpClient();
        gson = new Gson();
    }

    protected List<String> activateParticipants(Set<Integer> participantIds, String surveyId) {
        String sessionKey = getSessionKey();
        if (createParticipantTable(surveyId, sessionKey))
            return createParticipants(participantIds, surveyId, sessionKey);
        return new ArrayList<>();
    }

    private boolean createParticipantTable(String surveyId, String sessionKey) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "\"method\": \"activate_tokens\"," +
                                "\"params\": [" +
                                "\"sSessionKey\": \"" + sessionKey + "\"" +
                                "\"iSurveyID\": " + surveyId +
                                "]," +
                                "\"id\": 1"
                ))
                .build();
        try {
            if (parseToString(client.send(request, HttpResponse.BodyHandlers.ofString()).body()).contains("OK"))
                return true;
        } catch (IOException | InterruptedException ignored) {
        }
        return false;
    }

    private List<String> createParticipants(Set<Integer> participantIds, String surveyId, String sessionKey) {
        String participantData = String.join("},{", participantIds.stream().map(s -> toDataFormat(s.toString())).toList());
        HttpRequest createParticipantsRequest = HttpRequest.newBuilder()
                .uri(URI.create(getUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "\"method\": \"add_participants\"," +
                                "\"params\": [" +
                                "\"sSessionKey\": \"" + sessionKey + "\"" +
                                "\"iSurveyID\": " + surveyId +
                                "\"aParticipantData\": " +
                                "[ {" +
                                participantData +
                                "] }" +
                                "]," +
                                "\"id\": 1"
                ))
                .build();
        try {
            return parseToList(client.send(createParticipantsRequest, HttpResponse.BodyHandlers.ofString()).body())
                    .stream().map(
                            entry -> getId(entry) +
                                    "," +
                                    getToken(entry)
                    ).toList();
        } catch (IOException | InterruptedException e) {
            return new ArrayList<>();
        }
    }

    private String parseToString(String jsonResponse) {
        return gson
                .fromJson(jsonResponse, JsonObject.class)
                .get("result")
                .getAsString();
    }

    private List<String> parseToList(String jsonResponse) {
        JsonArray array = gson
                .fromJson(jsonResponse, JsonObject.class)
                .get("result")
                .getAsJsonArray();
        List<String> entries = new ArrayList<>();
        for (JsonElement el : array) {
            entries.add(el.getAsString());
        }
        return entries;
    }

    private String getId(String json) {
        return gson
                .fromJson(json, JsonObject.class)
                .get("firstname")
                .getAsString();
    }

    private String getToken(String json) {
        return gson
                .fromJson(json, JsonObject.class)
                .get("token")
                .getAsString();
    }


    private String toDataFormat(String id) {
        return "\"lastname\":" + id + "," +
                "\"firstname\":" + id;
    }

    private String getUrl() {
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
            HttpRequest listSurveysRequest = createHttpRequest(
                    String.format("{\"method\": \"list_surveys\", \"params\": [\"%s\", \"%s\"], \"id\": 1}",
                            getSessionKey(), username));
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
