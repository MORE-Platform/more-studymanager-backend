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
    void activateParticipantsTest(){
        //TODO
    }
}
