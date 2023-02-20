package io.redlink.more.studymanager.component.observation;

import com.google.gson.*;
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
    private final Gson gson;

    protected LimeSurveyRequestService(ComponentFactoryProperties properties){
        this.properties = properties;
        client = HttpClient.newHttpClient();
        gson = new Gson();
    }

    protected List<String> activateParticipants(Set<Integer> participantIds, String surveyId){
        String sessionKey = getSessionKey();
        if(createParticipantTable(surveyId, sessionKey))
            return createParticipants(participantIds, surveyId, sessionKey);
        return new ArrayList<>();
    }

    private boolean createParticipantTable(String surveyId, String sessionKey){
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "\"method\": \"activate_tokens\"," +
                                "\"params\": [" +
                                    "\"sSessionKey\": \""+sessionKey+"\"" +
                                    "\"iSurveyID\": "+ surveyId +
                                "]," +
                                "\"id\": 1"
                ))
                .build();
        try{
            if(parseToString(client.send(request, HttpResponse.BodyHandlers.ofString()).body()).contains("OK"))
                return true;
        }catch (IOException | InterruptedException ignored){}
        return false;
    }

    private List<String> createParticipants(Set<Integer> participantIds, String surveyId, String sessionKey){
        String participantData = String.join("},{", participantIds.stream().map(s -> toDataFormat(s.toString())).toList());
        HttpRequest createParticipantsRequest = HttpRequest.newBuilder()
                .uri(URI.create(getUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "\"method\": \"add_participants\"," +
                                "\"params\": [" +
                                    "\"sSessionKey\": \""+sessionKey+"\"" +
                                    "\"iSurveyID\": "+ surveyId +
                                    "\"aParticipantData\": " +
                                    "[ {" +
                                        participantData +
                                    "] }" +
                                "]," +
                                "\"id\": 1"
                ))
                .build();
        try{
            return parseToList(client.send(createParticipantsRequest, HttpResponse.BodyHandlers.ofString()).body())
                    .stream().map(
                            entry -> getId(entry) +
                                    "," +
                                    getToken(entry)
                    ).toList();
        }catch(IOException | InterruptedException e){
            return new ArrayList<>();
        }
    }

    private String getSessionKey(){
        HttpRequest sessionKeyRequest = HttpRequest.newBuilder()
                .uri(URI.create(getUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"method\": \"get_session_key\"," +
                                "\"params\": [" +
                                    "\"username\"" + properties.get("username").toString() + "," +
                                    "\"password\"" + properties.get("password").toString() +
                                "]," +
                                "\"id\": 1" +
                                "}"
                ))
                .build();
        try {
            return client.send(sessionKeyRequest, HttpResponse.BodyHandlers.ofString()).body();
        }catch(IOException | InterruptedException e){
            e.printStackTrace();
            return "";
        }
    }

    private String parseToString(String jsonResponse){
        return gson
                .fromJson(jsonResponse, JsonObject.class)
                .get("result")
                .getAsString();
    }

    private List<String> parseToList(String jsonResponse){
        JsonArray array = gson
                .fromJson(jsonResponse, JsonObject.class)
                .get("result")
                .getAsJsonArray();
        List<String> entries = new ArrayList<>();
        for(JsonElement el : array){
            entries.add(el.getAsString());
        }
        return entries;
    }

    private String getId(String json){
        return gson
                .fromJson(json, JsonObject.class)
                .get("firstname")
                .getAsString();
    }

    private String getToken(String json){
        return gson
                .fromJson(json, JsonObject.class)
                .get("token")
                .getAsString();
    }


    private String toDataFormat(String id){
        return "\"lastname\":" + id + "," +
                "\"firstname\":" + id;
    }

    private String getUrl(){
        return properties.get("url").toString();
    }
}
