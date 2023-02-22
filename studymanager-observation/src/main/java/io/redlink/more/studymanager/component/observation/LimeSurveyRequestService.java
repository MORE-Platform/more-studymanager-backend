package io.redlink.more.studymanager.component.observation;

import com.fasterxml.jackson.databind.ObjectWriter;
import io.redlink.more.studymanager.component.observation.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.more.studymanager.core.factory.ComponentFactoryProperties;

import java.io.IOException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
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
    private final ObjectMapper mapper = new ObjectMapper();
    private final ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
    private final TypeReference<Map<String, String>> mapStringStringRef
            = new TypeReference<>() {
    };
    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();


    protected LimeSurveyRequestService(ComponentFactoryProperties properties) {
        this.properties = properties;
        client = HttpClient.newHttpClient();
    }

    protected LimeSurveyRequestService(HttpClient client, ComponentFactoryProperties properties){
        this.properties = properties;
        this.client = client;
    }

    protected List<String> activateParticipants(Set<Integer> participantIds, String surveyId) {
        String sessionKey = getSessionKey();
        createParticipantTable(surveyId, sessionKey);
        return createParticipants(participantIds, surveyId, sessionKey);
    }

    protected void createParticipantTable(String surveyId, String sessionKey){
        LimeSurveyRequest request = new LimeSurveyRequest(
                "activate_tokens",
                List.of(sessionKey, surveyId),
                1
        );
        try{
            HttpRequest createTableRequest = HttpRequest.newBuilder()
                .uri(URI.create(getUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        objectWriter.writeValueAsString(request)
                ))
                .build();
            client.send(createTableRequest, HttpResponse.BodyHandlers.ofString()).body();
        }catch (IOException | InterruptedException ignored){}
    }

    protected List<String> createParticipants(Set<Integer> participantIds, String surveyId, String sessionKey){
        LimeSurveyRequest request = new LimeSurveyRequest(
                "add_participants",
                List.of(sessionKey, surveyId, participantIds.stream().map(i ->
                                new ParticipantData(i.toString(), i.toString())
                        ).toList()),
                1
        );
        try{
            HttpRequest createParticipantsRequest = HttpRequest.newBuilder()
                .uri(URI.create(getUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        objectWriter.writeValueAsString(request)
                )).build();
            return mapper.readValue(client.send(createParticipantsRequest, HttpResponse.BodyHandlers.ofString()).body(),
                            LimeSurveyListResponse.class)
                    .result().stream().map(
                            entry -> {
                                Map<String, String> entryMap = mapper.convertValue(entry, mapStringStringRef);
                                return entryMap.get("firstname") +
                                "," +
                                entryMap.get("token");
                            }
                    ).toList();
        } catch (IOException | InterruptedException e) {
            return new ArrayList<>();
        }
    }

    protected String getSessionKey(){
        LimeSurveyRequest request = new LimeSurveyRequest(
                "get_session_key",
                List.of(properties.get("username"), properties.get("password")),
                1
        );
        try {
            HttpRequest sessionKeyRequest = createHttpRequest(objectWriter.writeValueAsString(request));
            return mapper.readValue(
                    client.send(sessionKeyRequest, HttpResponse.BodyHandlers.ofString()).body(),
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
            LimeSurveyRequest request = new LimeSurveyRequest("list_surveys", List.of(getSessionKey(), username), 1);
            HttpRequest listSurveysRequest = createHttpRequest(objectWriter.writeValueAsString(request));
            LimeSurveyListResponse response =
                    mapper.readValue(client.send(listSurveysRequest, HttpResponse.BodyHandlers.ofString()).body(),
                            LimeSurveyListResponse.class);
            if (response.error() != null) {
                return mapper.convertValue(response.result(), JsonNode.class);
            }
            ArrayNode studies = mapper.convertValue(response.result(), ArrayNode.class);
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
                .uri(URI.create(getUrl()))
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
