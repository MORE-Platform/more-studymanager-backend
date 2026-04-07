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
import io.redlink.more.studymanager.component.observation.lime.model.ParticipantCreationData;
import io.redlink.more.studymanager.core.factory.ComponentFactoryProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;


public class LimeSurveyRequestServiceTest {

    private final ComponentFactoryProperties properties = new ComponentFactoryProperties();
    private final HttpClient client = mock(HttpClient.class);
    private final LimeSurveyRequestService service = new LimeSurveyRequestService(client, properties);

    @Test
    void parseRequestTest() throws JsonProcessingException {
        Assertions.assertEquals(service.parseRequest("get_session_key",
                List.of("username",
                        "password")),
                """
                        {"method":"get_session_key","params":["username","password"],"id":1}""");

        Assertions.assertEquals("""
                        {"method":"add_participants","params":[1,2,[{"firstname":"3","lastname":"4"},{"firstname":"5","lastname":"6"}]],"id":1}""",
                service.parseRequest("add_participants",
                        List.of(
                                1,
                                2,
                                List.of(
                                        new ParticipantCreationData("3", "4", null),
                                        new ParticipantCreationData("5", "6", null)))));

        Assertions.assertEquals(service.parseRequest("activate_tokens", List.of("1", "2")),
                """
                        {"method":"activate_tokens","params":["1","2"],"id":1}""");
    }

    @Test
    void fixNullDateTest() {
        Map<String, Object> answer = new HashMap<>();
        answer.put("Date submitted", "1980-01-01 00:00:00");
        answer.put("other", "value");

        service.fixNullDate(answer);

        Assertions.assertNotEquals("1980-01-01 00:00:00", answer.get("Date submitted"));
        Assertions.assertEquals("value", answer.get("other"));
        // Check if it's a valid date format
        Assertions.assertTrue(((String) answer.get("Date submitted")).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }
}
