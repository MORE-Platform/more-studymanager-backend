package io.redlink.more.studymanager.component.observation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.more.studymanager.core.exception.ApiCallException;
import io.redlink.more.studymanager.core.factory.ComponentFactoryProperties;
import io.redlink.more.studymanager.core.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(classes = LimeSurveyObservationFactory.class)
public class LimeSurveyObservationFactoryTest {

    @Autowired
    LimeSurveyObservationFactory factory;
    @MockBean
    LimeSurveyRequestService service;
    @MockBean
    ComponentFactoryProperties properties;

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void testListSurveysIsCalled() throws Exception {
        String slug = "surveys";
        User user = new User("username");
        JsonNode input = mapper.readTree("{}");
        factory.handleAPICall(slug, user, input);
        verify(service, times(1)).listSurveysByUser("username", null, null, null);
    }

    @Test
    void testSurveyIsNotCalled() throws Exception {
        String slug = "something-else";
        Exception exception = Assertions.assertThrows(ApiCallException.class,
                () -> factory.handleAPICall(slug, null, mapper.readTree("{}")));
        Assertions.assertEquals(exception.getMessage(), "Not found");
    }

    @Test
    void testListSurveysHasError() throws Exception {
        when(service.listSurveysByUser(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(mapper.readTree("{\"error\": \"some error\"}"));
        Assertions.assertThrows(ApiCallException.class,
                () -> factory.handleAPICall("", null, mapper.readTree("{}")));
    }
}
