package io.redlink.more.studymanager.component.observation;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.more.studymanager.component.observation.model.LimeSurveyKeyResponse;
import io.redlink.more.studymanager.component.observation.model.LimeSurveyRequest;
import io.redlink.more.studymanager.component.observation.model.LimeSurveyResponse;
import io.redlink.more.studymanager.component.observation.model.ParticipantData;
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
    private final ObjectMapper objectMapper;

    protected LimeSurveyRequestService(ComponentFactoryProperties properties){
        this.properties = properties;
        client = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
    }

    protected LimeSurveyRequestService(HttpClient client, ComponentFactoryProperties properties){
        this.properties = properties;
        this.client = client;
        objectMapper = new ObjectMapper();
    }

    protected List<String> activateParticipants(Set<Integer> participantIds, String surveyId){
        String sessionKey = getSessionKey();
        createParticipantTable(surveyId, sessionKey);
        return createParticipants(participantIds, surveyId, sessionKey);
    }

    protected void createParticipantTable(String surveyId, String sessionKey){
        LimeSurveyRequest request = new LimeSurveyRequest(
                "activate_tokens",
                List.of(
                        "SessionKey: " + sessionKey,
                        "SurveyId: " +   surveyId
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
                List.of(
                        "SessionKey: " + sessionKey,
                        "SurveyId: " + surveyId,
                        "ParticipantData: " + participantIds.stream().map(i ->
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
        }catch(IOException | InterruptedException e){
            return new ArrayList<>();
        }
    }

    protected String getSessionKey(){
        LimeSurveyRequest request = new LimeSurveyRequest(
                "get_session_key",
                List.of("username: " + properties.get("username"),
                        "password: " + properties.get("password")),
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
}
