/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.component.observation.lime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.more.studymanager.component.observation.lime.model.LimeSurveyListSurveyResponse;
import io.redlink.more.studymanager.component.observation.lime.model.LimeSurveyObjectResponse;
import io.redlink.more.studymanager.component.observation.lime.model.LimeSurveyParticipantCreationResponse;
import io.redlink.more.studymanager.component.observation.lime.model.LimeSurveyRequest;
import io.redlink.more.studymanager.component.observation.lime.model.ParticipantCreationData;
import io.redlink.more.studymanager.component.observation.lime.model.ParticipantData;
import io.redlink.more.studymanager.component.observation.lime.model.SurveyData;
import io.redlink.more.studymanager.component.observation.lime.transformer.ParticipantTransformer;
import io.redlink.more.studymanager.core.factory.ComponentFactoryProperties;
import io.redlink.more.studymanager.core.ui.OptionValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class LimeSurveyRequestService {

    private final ComponentFactoryProperties properties;
    private final HttpClient client;
    private static final String LIME_NULL_DATE = "1980-01-01 00:00:00";
    private static final DateTimeFormatter LIME_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ObjectMapper mapper = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(LimeSurveyRequestService.class);

    protected LimeSurveyRequestService(ComponentFactoryProperties properties) {
        this.properties = properties;
        client = HttpClient.newHttpClient();
    }

    protected LimeSurveyRequestService(HttpClient client, ComponentFactoryProperties properties) {
        this.properties = properties;
        this.client = client;
    }

    protected String getBaseUrl() {
        return Optional.ofNullable(properties.get("baseUrl")).map(String::valueOf).orElse(null);
    }

    protected void activateSurvey(String surveyId) {
        String sessionKey = null;
        try {
            sessionKey = getSessionKey();
            HttpRequest request = createHttpRequest(
                    parseRequest("activate_survey",
                            List.of(sessionKey, surveyId))
            );
            client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException e) {
            LOGGER.error("Error activating survey {}", surveyId, e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Interrupted while activating survey {}", surveyId, e);
            throw new RuntimeException(e);
        } finally {
            releaseSessionKeyQuietly(sessionKey);
        }
    }

    protected boolean deactivateSurvey(String surveyId) {
        String sessionKey = null;
        try {
            sessionKey = getSessionKey();
            HttpRequest request = createHttpRequest(
                    parseRequest("stop_survey",
                            List.of(sessionKey, surveyId))
            );
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = Optional.ofNullable(response.body()).orElse("").trim();

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                LOGGER.info("Could not stop LimeSurvey survey {} via remote control. HTTP status={}", surveyId, response.statusCode());
                return false;
            }

            if (responseBody.isBlank()) {
                LOGGER.info("Could not stop LimeSurvey survey {} via remote control because the response body was empty", surveyId);
                return false;
            }

            if (responseBody.startsWith("<") || responseBody.toLowerCase().contains("<html")) {
                LOGGER.info("LimeSurvey stop_survey returned HTML instead of JSON for survey {}. This usually means the endpoint is unsupported or redirected. body={}",
                        surveyId,
                        responseBody.substring(0, Math.min(responseBody.length(), 500)));
                return false;
            }

            JsonNode responseNode = mapper.readTree(responseBody);

            JsonNode errorNode = responseNode.path("error");
            if (!errorNode.isMissingNode() && !errorNode.isNull() && !errorNode.asText("").isBlank()) {
                LOGGER.info("LimeSurvey stop_survey is not available or failed for survey {}: {}", surveyId, errorNode.asText());
                return false;
            }

            JsonNode resultNode = responseNode.path("result");
            if (resultNode.isBoolean()) {
                return resultNode.asBoolean();
            }
            if (resultNode.isTextual()) {
                String resultText = resultNode.asText("").trim();
                return "OK".equalsIgnoreCase(resultText)
                        || "1".equals(resultText)
                        || "true".equalsIgnoreCase(resultText);
            }
            return !resultNode.isMissingNode() && !resultNode.isNull();
        } catch (IOException e) {
            LOGGER.info("Could not stop LimeSurvey survey {} via remote control", surveyId, e);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.info("Interrupted while stopping LimeSurvey survey {} via remote control", surveyId, e);
            return false;
        } finally {
            releaseSessionKeyQuietly(sessionKey);
        }
    }

    protected void deleteParticipants(String surveyId, Set<Integer> participantTokenIds) {
        if (participantTokenIds == null || participantTokenIds.isEmpty()) {
            return;
        }

        String sessionKey = null;
        try {
            sessionKey = getSessionKey();
            HttpRequest request = createHttpRequest(
                    parseRequest("delete_participants",
                            List.of(sessionKey, surveyId, participantTokenIds.stream().sorted().toList()))
            );
            String responseBody = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            JsonNode responseNode = mapper.readTree(responseBody);

            JsonNode errorNode = responseNode.path("error");
            if (!errorNode.isMissingNode() && !errorNode.isNull() && !errorNode.asText("").isBlank()) {
                throw new RuntimeException("Error deleting participants for survey " + surveyId + ": " + errorNode.asText());
            }

            LOGGER.info("Deleted {} LimeSurvey participants from survey {}", participantTokenIds.size(), surveyId);
        } catch (IOException e) {
            LOGGER.error("Error deleting participants for survey {}", surveyId, e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Interrupted while deleting participants for survey {}", surveyId, e);
            throw new RuntimeException(e);
        } finally {
            releaseSessionKeyQuietly(sessionKey);
        }
    }

    protected void setSurveyEndUrl(String surveyId, Long studyId, int observationId) {
        String sessionKey = null;
        try {
            sessionKey = getSessionKey();
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
            String body = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            LOGGER.debug("Set survey end url response for survey {}: {}", surveyId, body);
        } catch (IOException e) {
            LOGGER.error("Error setting ref url for survey {}", surveyId, e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Interrupted while setting ref url for survey {}", surveyId, e);
            throw new RuntimeException(e);
        } finally {
            releaseSessionKeyQuietly(sessionKey);
        }
    }

    private String getSurveyEndUrlQuery(Long studyId, int observationId) {
        return String.format("?savedid={SAVEDID}&surveyid={SID}&token={TOKEN}&studyId=%s&observationId=%s",
                studyId, observationId);
    }

    protected List<ParticipantData> activateParticipants(Set<ParticipantData.ParticipantInfo> participantIds, String surveyId) {
        String sessionKey = getSessionKey();
        createParticipantTable(surveyId, sessionKey);
        return createParticipants(participantIds, surveyId, sessionKey);
    }

    private void createParticipantTable(String surveyId, String sessionKey) {
        try {
            HttpRequest request = createHttpRequest(
                    parseRequest("activate_tokens",
                            List.of(sessionKey, surveyId))
            );
            client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Error creating participant table for survey {}", surveyId);
            throw new RuntimeException(e);
        }
    }

    public String getLanguage(String surveyId, String sessionKey) {
        try {
            HttpRequest request = createHttpRequest(
                    parseRequest("get_language_properties",
                            List.of(sessionKey, surveyId, List.of("surveyls_language")))
            );

            JsonNode node = mapper.readTree(client.send(request, HttpResponse.BodyHandlers.ofString()).body());
            JsonNode resultNode = node.path("result");
            JsonNode languageNode = resultNode.path("surveyls_language");

            if (languageNode.isMissingNode() || languageNode.isNull() || languageNode.asText().isBlank()) {
                throw new IllegalStateException("Missing survey language for survey " + surveyId);
            }

            return languageNode.asText();
        } catch (IOException e) {
            LOGGER.error("Error reading language for survey {}", surveyId, e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Interrupted while reading language for survey {}", surveyId, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * This method sets both firstname and lastname of lime-participants as the id of more-participants
     */
    private List<ParticipantData> createParticipants(Set<ParticipantData.ParticipantInfo> participantIds, String surveyId, String sessionKey) {
        if (participantIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            var participants = participantIds
                    .stream()
                    .map(parInfo ->
                            new ParticipantCreationData(parInfo.firstname(), parInfo.lastname(), null)
                    )
                    .toList();
            HttpRequest request = createHttpRequest(
                    parseRequest("add_participants",
                            List.of(
                                    sessionKey,
                                    surveyId,
                                    participants
                            )
                    )
            );
            LOGGER.info("sent {} participants to lime", participantIds.size());

            String rsp = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            releaseSessionKey(sessionKey);

            var result = mapper.readValue(
                    rsp,
                    LimeSurveyParticipantCreationResponse.class
            );

            if (result.error() != null && !result.error().trim().isEmpty()) {
                throw new RuntimeException("Could not connect to Limesurvey: " + result.error());
            }

            var data = result.result();

            LOGGER.debug("result: {}", data.stream().map(ParticipantCreationData::toString).collect(Collectors.joining()));
            return data.stream().map(ParticipantTransformer::transformToData).toList();
        } catch (IOException e) {
            LOGGER.error("Error creating participants for survey {}", surveyId, e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Interrupted while creating participants for survey {}", surveyId, e);
            throw new RuntimeException(e);
        }
    }

    private String getSessionKey() {
        try {
            HttpRequest request = createHttpRequest(
                    parseRequest("get_session_key",
                            List.of(properties.get("username"), properties.get("password")))
            );
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String rsp = Optional.ofNullable(response.body()).map(String::trim).orElse("");

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("LimeSurvey remote control returned HTTP " + response.statusCode()
                        + " for " + getRemoteUrl());
            }

            if (rsp.isBlank()) {
                LOGGER.error("Empty response from LimeSurvey remote control while getting session key. remoteUrl={}, status={}, username={}",
                        getRemoteUrl(), response.statusCode(), properties.get("username"));
                throw new RuntimeException("Empty response from LimeSurvey remote control. Check remoteUrl, reverse proxy, and LimeSurvey RemoteControl settings.");
            }

            LimeSurveyObjectResponse values = mapper.readValue(rsp, LimeSurveyObjectResponse.class);
            Object resultObject = values.result();
            String result = resultObject == null ? "" : resultObject.toString().trim();

            if (result.contains("Invalid user name or password")) {
                throw new RuntimeException("Not possible to get session key for Limesurvey because of invalid credentials.");
            } else if (result.contains("You have exceeded the number of maximum login attempts. Please wait 10 minutes before trying again")) {
                throw new RuntimeException("Too many login attempts for Limesurvey. Try again in 10 minutes.");
            }

            if (result.isBlank()) {
                LOGGER.error("LimeSurvey remote control returned an empty session key payload. remoteUrl={}, rawResponse={}", getRemoteUrl(), rsp);
                throw new RuntimeException("LimeSurvey returned an empty session key.");
            }

            return result;
        } catch (HttpTimeoutException e) {
            LOGGER.error("Timeout while getting session key from Limesurvey remote control at {}", getRemoteUrl(), e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            LOGGER.error("Error getting session key for Limesurvey remote control at {}", getRemoteUrl(), e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Interrupted while getting session key for Limesurvey remote control at {}", getRemoteUrl(), e);
            throw new RuntimeException(e);
        }
    }

    private void releaseSessionKey(String sessionKey) {
        if (sessionKey == null || sessionKey.isBlank()) {
            return;
        }
        try {
            HttpRequest request = createHttpRequest(
                    parseRequest("release_session_key",
                            List.of(sessionKey)));
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            LOGGER.error("Error releasing session key for Limesurvey remote control", e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Interrupted while releasing session key for Limesurvey remote control", e);
            throw new RuntimeException(e);
        }
    }

    private void releaseSessionKeyQuietly(String sessionKey) {
        if (sessionKey == null || sessionKey.isBlank()) {
            return;
        }
        try {
            releaseSessionKey(sessionKey);
        } catch (RuntimeException e) {
            LOGGER.warn("Could not release session key cleanly", e);
        }
    }

    protected String getRemoteUrl() {
        return properties.get("remoteUrl").toString();
    }

    public String getSurveyUrl() {
        return properties.get("surveyUrl").toString();
    }

    public JsonNode listSurveysByUser(String username, String filter, Integer start, Integer size) {
        String sessionKey = null;
        try {
            sessionKey = getSessionKey();
            HttpRequest listSurveysRequest = createHttpRequest(parseRequest("list_surveys", List.of(sessionKey, username)));
            LimeSurveyListSurveyResponse response =
                    mapper.readValue(client.send(listSurveysRequest, HttpResponse.BodyHandlers.ofString()).body(),
                            LimeSurveyListSurveyResponse.class);

            if (response.error() != null) {
                LOGGER.error("Error getting surveys for user: {}", response.error());
                throw new RuntimeException(response.error());
            }

            List<SurveyData> surveys = Optional.ofNullable(response.result()).orElse(List.of());
            List<SurveyData> result = surveys.stream()
                    .filter(res -> matchesFilter(filter, res.surveyTitle()))
                    .toList();

            return mapper.convertValue(
                    sublist(result, start, size).stream().map(d -> new OptionValue()
                            .setName(d.surveyTitle()).setValue(d.surveyId())).toList(),
                    JsonNode.class
            );
        } catch (IOException e) {
            LOGGER.error("Error getting surveys for user", e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Interrupted while getting surveys for user", e);
            throw new RuntimeException(e);
        } finally {
            releaseSessionKeyQuietly(sessionKey);
        }
    }

    public List<ParticipantData> listParticipants(String surveyId, Integer start, Integer limit) {
        String sessionKey = null;
        try {
            int effectiveStart = start == null ? 0 : Math.max(0, start);
            int effectiveLimit = limit == null ? 1000 : Math.max(1, limit);

            sessionKey = getSessionKey();
            HttpRequest request = createHttpRequest(
                    parseRequest("list_participants",
                            List.of(sessionKey, surveyId, effectiveStart, effectiveLimit, false))
            );

            String responseBody = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            JsonNode rootNode = mapper.readTree(responseBody);

            JsonNode errorNode = rootNode.path("error");
            if (!errorNode.isMissingNode() && !errorNode.isNull() && !errorNode.asText("").isBlank()) {
                throw new RuntimeException("Error listing participants for survey " + surveyId + ": " + errorNode.asText());
            }

            JsonNode resultNode = rootNode.path("result");
            if (resultNode.isMissingNode() || resultNode.isNull()) {
                return List.of();
            }

            if (resultNode.isArray()) {
                return mapper.convertValue(
                        resultNode,
                        mapper.getTypeFactory().constructCollectionType(List.class, ParticipantData.class)
                );
            }

            if (resultNode.isObject()) {
                List<ParticipantData> participants = new java.util.ArrayList<>();
                Iterator<JsonNode> elements = resultNode.elements();
                while (elements.hasNext()) {
                    JsonNode participantNode = elements.next();
                    if (participantNode != null && participantNode.isObject()) {
                        participants.add(mapper.treeToValue(participantNode, ParticipantData.class));
                    }
                }
                return participants;
            }

            LOGGER.warn("Unexpected list_participants result type for survey {}: {}", surveyId, resultNode.getNodeType());
            return List.of();
        } catch (IOException e) {
            LOGGER.error("Error querying participant list for survey {}", surveyId, e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Interrupted while querying participant list for survey {}", surveyId, e);
            throw new RuntimeException(e);
        } finally {
            releaseSessionKeyQuietly(sessionKey);
        }
    }

    private List<SurveyData> sublist(List<SurveyData> list, Integer start, Integer size) {
        if (start != null && start > list.size() - 1) {
            return List.of();
        }
        if (start != null && size != null) {
            return list.subList(start, Math.min(list.size(), start + size));
        }
        return list;
    }

    private HttpRequest createHttpRequest(String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(getRemoteUrl()))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
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

    private boolean matchesFilter(String filter, String text) {
        return Optional.ofNullable(filter)
                .filter(f -> !f.isBlank())
                .map(f -> Optional.ofNullable(text)
                        .map(t -> t.toLowerCase().contains(f.toLowerCase()))
                        .orElse(false))
                .orElse(true);
    }

    public Optional<Map<String, Object>> getAnswer(String token, int surveyId, int savedId) {
        return getAnswer(token, surveyId, savedId, "code", "short");
    }

    public Optional<Map<String, Object>> getAnswerPlaintext(String token, int surveyId, int savedId) {
        return getAnswer(token, surveyId, savedId, "full", "long");
    }

    private Optional<Map<String, Object>> getAnswer(String token, int surveyId, int savedId, String headingType, String responseType) {
        if (token == null || token.isBlank() || surveyId <= 0 || savedId <= 0) {
            LOGGER.warn("Invalid answer query parameters: surveyId={}, savedId={}", surveyId, savedId);
            return Optional.empty();
        }

        String sessionKey = null;
        try {
            sessionKey = getSessionKey();
            String lang = getLanguage(String.valueOf(surveyId), sessionKey);
            HttpRequest request = createHttpRequest(
                    parseRequest("export_responses_by_token",
                            List.of(sessionKey, surveyId, "json", token, lang, "all", headingType, responseType))
            );
            var responseBody = client.send(request, HttpResponse.BodyHandlers.ofString()).body();

            JsonNode responseNode = mapper.readTree(responseBody);
            JsonNode errorNode = responseNode.path("error");
            if (!errorNode.isMissingNode() && !errorNode.isNull() && !errorNode.asText("").isBlank()) {
                LOGGER.warn("LimeSurvey returned an error for survey {} and savedId {}: {}", surveyId, savedId, errorNode.asText());
                return Optional.empty();
            }

            JsonNode resultNode = responseNode.path("result");
            if (!resultNode.isTextual() || resultNode.asText().isBlank()) {
                return Optional.empty();
            }

            JsonNode result = mapper.readTree(Base64.getDecoder().decode(resultNode.asText()));
            JsonNode responsesNode = result.path("responses");
            if (!responsesNode.isArray()) {
                return Optional.empty();
            }

            Iterator<JsonNode> responses = responsesNode.elements();
            while (responses.hasNext()) {
                JsonNode response = responses.next();
                if (response == null || !response.isObject()) {
                    continue;
                }

                Map<String, Object> answer = mapper.convertValue(response, Map.class);
                //NOTE: Do not store the survey token
                answer.remove("token");
                answer.values().removeIf(obj -> Objects.isNull(obj) || obj.equals(token));

                fixNullDate(answer);

                Object responseId = answer.get("Response ID");
                Object id = answer.get("id");
                boolean matchesSavedId = Objects.equals(String.valueOf(savedId), String.valueOf(responseId))
                        || Objects.equals(String.valueOf(savedId), String.valueOf(id));

                if (matchesSavedId || responsesNode.size() == 1) {
                    return Optional.of(new HashMap<>(answer));
                }
            }
            return Optional.empty();
        } catch (IllegalArgumentException e) {
            LOGGER.error("Could not decode LimeSurvey response payload for survey {} and savedId {}", surveyId, savedId, e);
            return Optional.empty();
        } catch (IOException e) {
            LOGGER.error("Error reading results for {}", surveyId, e);
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Interrupted while reading results for {}", surveyId, e);
            return Optional.empty();
        } finally {
            releaseSessionKeyQuietly(sessionKey);
        }
    }

    protected void fixNullDate(Map<String, Object> answer) {
        answer.replaceAll((key, value) -> {
            if (LIME_NULL_DATE.equals(value)) {
                return LocalDateTime.now().format(LIME_DATE_FORMATTER);
            }
            return value;
        });
    }

}
