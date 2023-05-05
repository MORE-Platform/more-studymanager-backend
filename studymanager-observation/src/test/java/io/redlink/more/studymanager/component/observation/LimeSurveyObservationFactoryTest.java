package io.redlink.more.studymanager.component.observation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.more.studymanager.core.exception.ApiCallException;
import io.redlink.more.studymanager.core.factory.ComponentFactoryProperties;
import io.redlink.more.studymanager.core.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(classes = LimeSurveyObservationFactory.class)
public class LimeSurveyObservationFactoryTest {


    @MockBean
    LimeSurveyRequestService service;
    @MockBean
    ComponentFactoryProperties properties;
    ObjectMapper mapper = new ObjectMapper();

    @Test
    void testListSurveysIsCalled() throws Exception {
        LimeSurveyObservationFactory factory = new LimeSurveyObservationFactory(properties, service);
        String slug = "surveys";
        User user = new User("username");
        JsonNode input = mapper.readTree("{}");
        factory.handleAPICall(slug, user, input);
        verify(service, times(1)).listSurveysByUser("username", null, 0, 10);
    }

    @Test
    void testSurveyIsNotCalled() throws Exception {
        LimeSurveyObservationFactory factory = new LimeSurveyObservationFactory(properties, service);
        String slug = "something-else";
        Exception exception = Assertions.assertThrows(ApiCallException.class,
                () -> factory.handleAPICall(slug, null, mapper.readTree("{}")));
        Assertions.assertEquals(exception.getMessage(), "Not found");
    }

    @Test
    void testListSurveysHasError() throws Exception {
        LimeSurveyObservationFactory factory = new LimeSurveyObservationFactory(properties, service);
        when(service.listSurveysByUser(anyString(), anyString(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("some error"));
        ApiCallException exception = Assertions.assertThrows(ApiCallException.class,
                () -> factory.handleAPICall("surveys", new User("username"), mapper.readTree("{\"filter\":  \"some-filter\", \"size\": 0, \"start\":  0}")));
        Assertions.assertEquals(500, exception.getStatus());
    }
}
