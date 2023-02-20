package io.redlink.more.studymanager.component.observation;

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

public class LimeSurveyRequestService {

    private static LimeSurveyRequestService instance;
    private static HttpClient client;
    private final ObjectMapper mapper = new ObjectMapper();
    private final TypeReference<Map<String, String>> mapStringStringRef
            = new TypeReference<>() {
    };

    private LimeSurveyRequestService() {
        client = HttpClient.newHttpClient();
    }

    public static LimeSurveyRequestService getInstance() {
        if (LimeSurveyRequestService.instance == null) {
            LimeSurveyRequestService.instance = new LimeSurveyRequestService();
        }
        return LimeSurveyRequestService.instance;
    }

    private String getSessionKey() {
        HttpRequest sessionKeyRequest = createHttpRequest(
                "{\"method\": \"get_session_key\", \"params\": [\"more-admin\", \"\"], \"id\": 1}");
        try {
            String response = client.send(sessionKeyRequest, HttpResponse.BodyHandlers.ofString()).body();
            return mapper.readValue(response, mapStringStringRef).get("result");
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> transformResultToMap(String content) throws JsonProcessingException {
        TypeReference<Map<String, Object>> typeRef
                = new TypeReference<>() {
        };
        return mapper.readValue(content, typeRef);
    }

    public JsonNode listSurveysByUser(String username, String filter, Integer start, Integer size) {
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

    private List<Map<String, Object>> handlePagination(List<Map<String, Object>> list, Integer start, Integer size) {
        if (start != null && size != null) {
            List<Map<String, Object>> sublist = list.subList(start, list.size());
            list = size < sublist.size() ? sublist.subList(0, size) : sublist;
        }
        return list;
    }

    private HttpRequest createHttpRequest(String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create("https://lime.platform-test.more.redlink.io/admin/remotecontrol"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest
                        .BodyPublishers
                        .ofString(body))
                .build();
    }

    private boolean isInFilter(String filter, String text) {
        if (filter == null)
            return true;
        return text.toLowerCase().contains(filter.toLowerCase());
    }
}
