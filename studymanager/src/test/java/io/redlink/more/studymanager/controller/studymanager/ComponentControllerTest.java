/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.controller.studymanager;

import com.fasterxml.jackson.databind.JsonNode;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.model.User;
import io.redlink.more.studymanager.model.AuthenticatedUser;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ComponentApiV1Controller.class})
@AutoConfigureMockMvc(addFilters = false)
class ComponentControllerTest {

    @MockitoBean
    private OAuth2AuthenticationService authenticationService;

    @MockitoBean(name = "my-test-observation")
    private ObservationFactory factory;

    @Autowired
    private MockMvc mvc;

    @Captor
    ArgumentCaptor<JsonNode> jsonNodeArgumentCaptor;

    @Test
    void testComponentSpecificEndpointExists() throws Exception {

        AuthenticatedUser user = new AuthenticatedUser("user1", "", "","", Set.of());
        when(authenticationService.getCurrentUser()).thenReturn(user);

        mvc.perform(MockMvcRequestBuilders.post("/api/v1/components/observation/my-test-observation/api/my-test-slug")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"hello\":\"world\"}"))
                .andExpect(status().isOk());

        verify(factory).handleAPICall(anyString(), any(User.class), jsonNodeArgumentCaptor.capture());
        String value = jsonNodeArgumentCaptor.getValue().get("hello").asText();
        Assertions.assertEquals("world", value);
    }

    @Test
    void testComponentSpecificEndpointDoesNotExist() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/api/v1/components/observation/another-test-observation/api/my-test-slug")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

}
