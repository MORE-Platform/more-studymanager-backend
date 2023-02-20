package io.redlink.more.studymanager.component.observation;

import io.redlink.more.studymanager.core.factory.ComponentFactoryProperties;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LimeSurveyRequestService {

    private final ComponentFactoryProperties properties;
    private final HttpClient client;

    protected LimeSurveyRequestService(ComponentFactoryProperties properties){
        this.properties = properties;
        client = HttpClient.newHttpClient();
    }

    protected List<String> createParticipants(Set<Integer> participantIds, long studyId){
        String participantData = String.join("},{", participantIds.stream().map(s -> toDataFormat(s.toString())).toList());
        HttpRequest createParticipantsRequest = HttpRequest.newBuilder()
                .uri(URI.create(getUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "\"method\": \"add_participants\"," +
                                "\"params\": [" +
                                    "\"sSessionKey\": \""+getSessionKey()+"\"" +
                                    "\"iSurveyID\": "+ studyId +
                                    "\"aParticipantData\": " +
                                    "[ {" +
                                        participantData +
                                    "] }" +
                                "]," +
                                "\"id\": 1"
                ))
                .build();
        try{
            return client.send(createParticipantsRequest, HttpResponse.BodyHandlers.ofLines()).body().collect(Collectors.toList());
        }catch(IOException | InterruptedException e){
            e.printStackTrace();
            return null;
        }
    }

    private String getSessionKey(){
        HttpRequest sessionKeyRequest = HttpRequest.newBuilder()
                .uri(URI.create(getUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "\"method\": \"get_session_key\"," +
                                "\"params\": [" +
                                    "\"username\"" + properties.get("username").toString() + "," +
                                    "\"password\"" + properties.get("password").toString() +
                                "]," +
                                "\"id\": 1"
                ))
                .build();
        try {
            return client.send(sessionKeyRequest, HttpResponse.BodyHandlers.ofString()).body();
        }catch(IOException | InterruptedException e){
            e.printStackTrace();
            return "";
        }
    }

    private String toDataFormat(String id){
        return "\"email\":" + id + "," +
                "\"lastname\":" + id + "," +
                "\"firstname\":" + id;
    }

    private String getUrl(){
        return properties.get("url").toString();
    }

}
