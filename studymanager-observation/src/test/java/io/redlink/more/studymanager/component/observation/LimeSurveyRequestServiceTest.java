package io.redlink.more.studymanager.component.observation;

import io.redlink.more.studymanager.core.factory.ComponentFactoryProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import java.net.http.HttpClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(classes = LimeSurveyRequestService.class)
public class LimeSurveyRequestServiceTest {

    @MockBean
    ComponentFactoryProperties properties;
    @MockBean
    HttpClient client;
    @Autowired
    LimeSurveyRequestService service;

    @Test
    void testListSurveysByUser() throws Exception {
        System.out.println();
        service.listSurveysByUser("", "", 0, 0);
    }
}
