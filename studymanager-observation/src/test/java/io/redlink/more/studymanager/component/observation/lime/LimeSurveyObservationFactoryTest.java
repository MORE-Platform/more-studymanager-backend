/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.component.observation.lime;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.more.studymanager.core.exception.ApiCallException;
import io.redlink.more.studymanager.core.factory.ComponentFactoryProperties;
import io.redlink.more.studymanager.core.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(classes = LimeSurveyObservationFactory.class)
class LimeSurveyObservationFactoryTest {


    @MockitoBean
    LimeSurveyRequestService service;
    @MockitoBean
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
    void testSurveyIsNotCalled() {
        LimeSurveyObservationFactory factory = new LimeSurveyObservationFactory(properties, service);
        String slug = "something-else";
        Exception exception = Assertions.assertThrows(ApiCallException.class,
                () -> factory.handleAPICall(slug, null, mapper.readTree("{}")));
        Assertions.assertEquals("Not found", exception.getMessage());
    }

    @Test
    void testListSurveysHasError() {
        LimeSurveyObservationFactory factory = new LimeSurveyObservationFactory(properties, service);
        when(service.listSurveysByUser(anyString(), anyString(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("some error"));
        ApiCallException exception = Assertions.assertThrows(ApiCallException.class,
                () -> factory.handleAPICall("surveys", new User("username"), mapper.readTree("{\"filter\":  \"some-filter\", \"size\": 0, \"start\":  0}")));
        Assertions.assertEquals(500, exception.getStatus());
    }
}
