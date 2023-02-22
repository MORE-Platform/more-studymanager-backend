package io.redlink.more.studymanager.component.observation;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.redlink.more.studymanager.component.observation.model.ParticipantData;
import io.redlink.more.studymanager.core.factory.ComponentFactoryProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class LimeSurveyRequestServiceTest {

    private final ComponentFactoryProperties properties = new ComponentFactoryProperties();
    private final HttpClient client = mock(HttpClient.class);
    private final LimeSurveyRequestService service = new LimeSurveyRequestService(client, properties);

    @Test
    void parseRequestTest() throws JsonProcessingException {
        System.out.println(service.parseRequest("get_session_key",
                Map.of("username", "username",
                        "password", "password"),
                1));

        System.out.println(service.parseRequest("add_participants",
                        Map.of(
                                "SessionKey", 123,
                                "SurveyId", 123,
                                "ParticipantData", List.of(
                                        new ParticipantData("123", "456"),
                                        new ParticipantData("123", "456"))),
                        1));

        System.out.println(service.parseRequest("get_session_key",
                Map.of("username", "username",
                        "password", "password"),
                1));
    }
}
