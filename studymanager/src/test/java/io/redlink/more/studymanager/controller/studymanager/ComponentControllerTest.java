/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.controller.studymanager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.redlink.more.studymanager.core.factory.ActionFactory;
import io.redlink.more.studymanager.core.factory.GoalTemplateFactory;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.factory.TriggerFactory;
import io.redlink.more.studymanager.core.io.Visibility;
import io.redlink.more.studymanager.core.measurement.MeasurementSet;
import io.redlink.more.studymanager.core.model.User;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.properties.model.BooleanValue;
import io.redlink.more.studymanager.core.properties.model.IntegerRange;
import io.redlink.more.studymanager.core.properties.model.IntegerRangeValue;
import io.redlink.more.studymanager.core.properties.model.IntegerValue;
import io.redlink.more.studymanager.core.properties.model.StringTextValue;
import io.redlink.more.studymanager.core.properties.model.StringValue;
import io.redlink.more.studymanager.model.AuthenticatedUser;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ComponentApiV1Controller.class})
@AutoConfigureMockMvc(addFilters = false)
class ComponentControllerTest {

    private static ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());


    @MockitoBean
    private OAuth2AuthenticationService authenticationService;

    @Autowired
    private MockMvc mvc;

    //Test configuration that registers a observation-, trigger-, action- and goalTemplateFactory for testing
    @TestConfiguration
    static class TestComponentConfig {

        ObservationFactory observationFactory;
        TriggerFactory triggerFactory;
        ActionFactory actionFactory;
        GoalTemplateFactory goalTemplateFactory;

        public TestComponentConfig() {
            this.observationFactory = mock(ObservationFactory.class);
            when(observationFactory.getId()).thenReturn("my-test-observation");
            when(observationFactory.getPropertyClass()).thenReturn(ObservationProperties.class);
            when(observationFactory.getProperties()).thenReturn(List.of(
                    new BooleanValue("test-boolean")
                            .setName("Boolean Value Test")
                            .setRequired(true)
                            .setImmutable(false)
                            .setDefaultValue(false),
                    new IntegerValue("test-integer")
                            .setName("Integer Value Test")
                            .setRequired(true)
                            .setImmutable(false)
                            .setDefaultValue(-1),
                    new StringValue("test-string")
                            .setName("String Value Test")
                            .setRequired(true)
                            .setImmutable(false)
                            .setDefaultValue("default"),
                    new StringTextValue("test-text")
                            .setName("Text Value Test")
                            .setRequired(true)
                            .setImmutable(false)
                            .setDefaultValue("default\nmultiline"),
                    new IntegerRangeValue("test-range")
                            .setMin(0)
                            .setMax(100)
                            .setName("Range Value Test")
                            .setRequired(true)
                            .setImmutable(false)
                            .setDefaultValue(new IntegerRange(1, 50))
            ));

            when(observationFactory.getMeasurementSet()).thenReturn(new MeasurementSet("TEST", Set.of()));
            when(observationFactory.getVisibility()).thenReturn(Visibility.DEFAULT);
            when(observationFactory.isResyncable()).thenReturn(true);

            this.triggerFactory = mock(TriggerFactory.class);
            when(triggerFactory.getId()).thenReturn("my-test-trigger");

            this.actionFactory = mock(ActionFactory.class);
            when(actionFactory.getId()).thenReturn("my-test-action");

            this.goalTemplateFactory = mock(GoalTemplateFactory.class);
            when(goalTemplateFactory.getId()).thenReturn("my-test-goal-template");
        }

        @Bean("my-test-observation")
        public ObservationFactory getObservationFactory() {
            return observationFactory;
        }

        @Bean("my-test-trigger")
        public TriggerFactory getTriggerFactory() {
            return triggerFactory;
        }

        @Bean("my-test-action")
        public ActionFactory getActionFactory() {
            return actionFactory;
        }

        @Bean("my-test-goal-template")
        public GoalTemplateFactory getGoalTemplateFactory() {
            return goalTemplateFactory;
        }
    }

    @Autowired
    private TestComponentConfig testComponentConfig;

    @Captor
    ArgumentCaptor<JsonNode> jsonNodeArgumentCaptor;

    @Test
    void testComponentSpecificEndpointExists() throws Exception {

        AuthenticatedUser user = new AuthenticatedUser("user1", "", "", "", Set.of());
        when(authenticationService.getCurrentUser()).thenReturn(user);

        mvc.perform(MockMvcRequestBuilders.post("/api/v1/components/observation/my-test-observation/api/my-test-slug")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"hello\":\"world\"}"))
                .andExpect(status().isOk());

        verify(testComponentConfig.observationFactory).handleAPICall(anyString(), any(User.class), jsonNodeArgumentCaptor.capture());
        String value = jsonNodeArgumentCaptor.getValue().get("hello").asText();
        Assertions.assertEquals("world", value);

        mvc.perform(MockMvcRequestBuilders.post("/api/v1/components/action/my-test-action/api/my-test-slug")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"hello\":\"world\"}"))
                .andExpect(status().isOk());

        verify(testComponentConfig.observationFactory).handleAPICall(anyString(), any(User.class), jsonNodeArgumentCaptor.capture());
        value = jsonNodeArgumentCaptor.getValue().get("hello").asText();
        Assertions.assertEquals("world", value);

        mvc.perform(MockMvcRequestBuilders.post("/api/v1/components/trigger/my-test-trigger/api/my-test-slug")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"hello\":\"world\"}"))
                .andExpect(status().isOk());

        verify(testComponentConfig.observationFactory).handleAPICall(anyString(), any(User.class), jsonNodeArgumentCaptor.capture());
        value = jsonNodeArgumentCaptor.getValue().get("hello").asText();
        Assertions.assertEquals("world", value);

        mvc.perform(MockMvcRequestBuilders.post("/api/v1/components/goalTemplate/my-test-goal-template/api/my-test-slug")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"hello\":\"world\"}"))
                .andExpect(status().isOk());

        verify(testComponentConfig.observationFactory).handleAPICall(anyString(), any(User.class), jsonNodeArgumentCaptor.capture());
        value = jsonNodeArgumentCaptor.getValue().get("hello").asText();
        Assertions.assertEquals("world", value);

    }

    @Test
    void testListObservationComponentsExposesResyncable() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/api/v1/components/observation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].componentId").value("my-test-observation"))
                .andExpect(jsonPath("$[0].resyncable").value(true))
                .andExpect(jsonPath("$[0].visibility.changeable").value(true));
    }

    @Test
    void testComponentSpecificEndpointDoesNotExist() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/api/v1/components/observation/another-test-observation/api/my-test-slug")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{}"))
                .andExpect(status().isNotFound());

        mvc.perform(MockMvcRequestBuilders.post("/api/v1/components/trigger/another-test-trigger/api/my-test-slug")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{}"))
                .andExpect(status().isNotFound());

        mvc.perform(MockMvcRequestBuilders.post("/api/v1/components/action/another-test-action/api/my-test-slug")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{}"))
                .andExpect(status().isNotFound());

        mvc.perform(MockMvcRequestBuilders.post("/api/v1/components/goalTemplate/another-test-goal-template/api/my-test-slug")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{}"))
                .andExpect(status().isNotFound());

    }

    @Test
    public void testComponentProperties() throws Exception {

        Map<String, Object> properties = new HashMap<>();
        properties.put("test-boolean", true);
        properties.put("test-integer", 1);
        properties.put("test-string", "test");
        properties.put("test-text", "test\nmultiline");
        properties.put("test-range", Map.of("lower", 1, "upper", 10));
        String content = MAPPER.writeValueAsString(properties);
        AuthenticatedUser user = new AuthenticatedUser("user1", "", "", "", Set.of());
        when(authenticationService.getCurrentUser()).thenReturn(user);
        mvc.perform(MockMvcRequestBuilders.post("/api/v1/components/observation/my-test-observation/validate")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(status().isOk());


    }


}
