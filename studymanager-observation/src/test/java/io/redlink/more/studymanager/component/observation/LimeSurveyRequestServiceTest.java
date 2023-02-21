package io.redlink.more.studymanager.component.observation;

import io.redlink.more.studymanager.core.factory.ComponentFactoryProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;

import static org.mockito.Mockito.mock;


public class LimeSurveyRequestServiceTest {

    private final ComponentFactoryProperties properties = new ComponentFactoryProperties();
    private final HttpClient client = mock(HttpClient.class);
    private final LimeSurveyRequestService service = new LimeSurveyRequestService(client, properties);

    @Test
    void getIdTest() {
        String string = "{" +
                "\"firstname\": \"3\"," +
                "\"token\": \"123\"" +
                "}";
        Assertions.assertEquals(service.getId(string), "3");
    }

    @Test
    void parseToStringTest() {
        String string = "{" +
                "\"test1\": \"3\"," +
                "\"test2\": \"123\"," +
                "\"result\": {" +
                "\"id\": \"1\","+
                "\"token\": \"2\" }" +
                "}";
        Assertions.assertEquals(service.getToken(service.parseToString(string)), "2");
    }

    @Test
    void parseToListTest() {
        String string = "{" +
                "\"test1\": \"3\"," +
                "\"test2\": \"123\"," +
                "\"result\": [" +
                "{" +
                "\"id\": \"1\","+
                "\"token\": \"2\" " +
                "}," +
                "{" +
                "\"id\": \"2\","+
                "\"token\": \"3\"" +
                "}" +
                "]" +
                "}";
        Assertions.assertEquals(service.parseToList(string).size(), 2);
        Assertions.assertEquals(service.parseToList(string).get(0), "{" +
                "\"id\":\"1\","+
                "\"token\":\"2\"" +
                "}");
        Assertions.assertEquals(service.parseToList(string).get(1), "{" +
                "\"id\":\"2\","+
                "\"token\":\"3\"" +
                "}");
    }
}
