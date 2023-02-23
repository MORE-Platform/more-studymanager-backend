package io.redlink.more.studymanager.component.observation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.redlink.more.studymanager.component.observation.model.*;
import io.redlink.more.studymanager.core.factory.ComponentFactoryProperties;

import java.io.IOException;
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
    private final TypeReference<Map<String, String>> mapStringStringRef
            = new TypeReference<>() {
    };

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
        }catch (IOException | InterruptedException ignored){}
    }

    /**
     * This method sets both firstname and lastname of lime-participants as the id of more-participants
     */
    protected List<ParticipantData> createParticipants(Set<Integer> participantIds, String surveyId, String sessionKey){
        try{
            HttpRequest request = createHttpRequest(
                        parseRequest("add_participants",
                                List.of(sessionKey, surveyId, participantIds.stream().map(i ->
                                                new ParticipantData(i.toString(), i.toString(), null)
                                        ).toList()))
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
                                List.of(properties.get("username"), properties.get("password")))
                );
            return mapper.readValue(
                    client.send(request, HttpResponse.BodyHandlers.ofString()).body(),
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

    public JsonNode listSurveysByUser (String username, String filter, Integer start, Integer size){
        try {
            LimeSurveyRequest request = new LimeSurveyRequest("list_surveys", List.of(getSessionKey(), username), 1);
            HttpRequest listSurveysRequest = createHttpRequest(mapper.writeValueAsString(request));
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
