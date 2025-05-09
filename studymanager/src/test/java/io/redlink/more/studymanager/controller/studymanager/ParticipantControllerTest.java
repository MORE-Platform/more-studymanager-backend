/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.controller.studymanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.more.studymanager.api.v1.model.ParticipantDTO;
import io.redlink.more.studymanager.model.AuthenticatedUser;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.PlatformRole;
import io.redlink.more.studymanager.properties.GatewayProperties;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import io.redlink.more.studymanager.service.ParticipantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.EnumSet;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest({ParticipantsApiV1Controller.class})
@AutoConfigureMockMvc(addFilters = false)
class ParticipantControllerTest {
    public static final String SIGNUP_URL = "https://example.com";

    @MockBean
    ParticipantService participantService;
    @MockBean
    OAuth2AuthenticationService oAuth2AuthenticationService;

    @Autowired
    private GatewayProperties gatewayProperties;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @TestConfiguration
    static class GatewayPropertiesConfigurationTest {

        @Bean
        GatewayProperties gatewayProperties() {
            return new GatewayProperties(SIGNUP_URL);
        }
    }

    @BeforeEach
    void setUp() {
        when(oAuth2AuthenticationService.getCurrentUser()).thenReturn(
                new AuthenticatedUser(
                        UUID.randomUUID().toString(),
                        "Test User", "test@example.com", "Test Inc.",
                        EnumSet.allOf(PlatformRole.class)
                )
        );
    }

    @Test
    @DisplayName("Create participant should create the participant with Id and status set")
    void testCreateParticipant() throws Exception {

        final long studyId = 1L;

        when(participantService.createParticipant(any(Participant.class))).thenAnswer(invocationOnMock -> new Participant()
                .setStudyId(1L)
                .setParticipantId(1)
                .setAlias("participant x")
                .setStudyGroupId(1)
                .setStatus(Participant.Status.NEW)
                .setCreated(Instant.ofEpochMilli(System.currentTimeMillis()))
                .setModified(Instant.ofEpochMilli(System.currentTimeMillis()))
                .setRegistrationToken("TEST123"));

        ParticipantDTO participantRequest = new ParticipantDTO()
                .studyId(studyId)
                .alias("participant x")
                .studyGroupId(1);

        ParticipantDTO[] participantDTOS = new ParticipantDTO[]{participantRequest};

        mvc.perform(post("/api/v1/studies/{studyId}/participants", studyId)
                        .content(mapper.writeValueAsString(participantDTOS))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].studyId").value(participantRequest.getStudyId()))
                .andExpect(jsonPath("$[0].participantId").value(1))
                .andExpect(jsonPath("$[0].status").value("new"))
                .andExpect(jsonPath("$[0].studyGroupId").value(participantRequest.getStudyGroupId()))
                .andExpect(jsonPath("$[0].registrationToken").exists());
    }

    @Test
    @DisplayName("Participants can be created from csv")
    void testCreateFromCSV() throws Exception {
        String participant1 = "Participant1";
        String participant2 = "Participant2";
        when(participantService.createParticipant(any(Participant.class))).thenAnswer(invocationOnMock -> new Participant()
                        .setStudyId(1L)
                        .setParticipantId(1)
                        .setAlias(participant1)
                        .setStudyGroupId(1)
                        .setStatus(Participant.Status.NEW)
                        .setCreated(Instant.ofEpochMilli(System.currentTimeMillis()))
                        .setModified(Instant.ofEpochMilli(System.currentTimeMillis()))
                        .setRegistrationToken("TEST123"))
                .thenAnswer(invocationOnMock -> new Participant()
                        .setStudyId(1L)
                        .setParticipantId(1)
                        .setAlias(participant2)
                        .setStudyGroupId(1)
                        .setStatus(Participant.Status.NEW)
                        .setCreated(Instant.ofEpochMilli(System.currentTimeMillis()))
                        .setModified(Instant.ofEpochMilli(System.currentTimeMillis()))
                        .setRegistrationToken("TEST123"));

        String csv = "Participants\n%s\n%s".formatted(participant1, participant2);
        mvc.perform(post("/api/v1/studies/{studyId}/participants", 1L)
                        .content(csv)
                        .contentType(new MediaType("text", "csv")))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Update participant should return similar values")
    void testUpdateStudy() throws Exception {
        when(participantService.updateParticipant(any(Participant.class))).thenAnswer(invocationOnMock ->
                invocationOnMock.getArgument(0, Participant.class)
                        .setStatus(Participant.Status.NEW)
                        .setAlias("person x")
                        .setCreated(Instant.ofEpochMilli(0))
                        .setModified(Instant.ofEpochMilli(0))
                        .setRegistrationToken("TEST123"));

        ParticipantDTO participantRequest = new ParticipantDTO()
                .studyId(1L)
                .participantId(1)
                .studyGroupId(1)
                .alias("person x");

        mvc.perform(put("/api/v1/studies/1/participants/1")
                        .content(mapper.writeValueAsString(participantRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studyId").value(participantRequest.getStudyId()))
                .andExpect(jsonPath("$.participantId").value(participantRequest.getParticipantId()))
                .andExpect(jsonPath("$.studyGroupId").value(participantRequest.getStudyGroupId()))
                .andExpect(jsonPath("$.alias").value(participantRequest.getAlias()))
                .andExpect(jsonPath("$.modified").exists())
                .andExpect(jsonPath("$.created").exists());
    }

    @Test
    @DisplayName("The created signup URI of an participant with a token is correct")
    void testRegistrationUri() throws Exception {
        String token = "laskdfjlkasdfj1z239i1uf";
        when(participantService.getParticipant(1L, 1)).thenAnswer(invocationOnMock ->
                new Participant()
                        .setStudyId(invocationOnMock.getArgument(0, Long.class))
                        .setParticipantId(invocationOnMock.getArgument(1, Integer.class))
                        .setStudyGroupId(1)
                        .setStatus(Participant.Status.NEW)
                        .setAlias("person x")
                        .setCreated(Instant.ofEpochMilli(0))
                        .setModified(Instant.ofEpochMilli(0))
                        .setRegistrationToken(token));

        ParticipantDTO participantRequest = new ParticipantDTO()
                .studyId(1L)
                .participantId(1)
                .studyGroupId(1)
                .alias("person x");

        mvc.perform(get("/api/v1/studies/1/participants/1")
                        .content(mapper.writeValueAsString(participantRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studyId").value(participantRequest.getStudyId()))
                .andExpect(jsonPath("$.participantId").value(participantRequest.getParticipantId()))
                .andExpect(jsonPath("$.studyGroupId").value(participantRequest.getStudyGroupId()))
                .andExpect(jsonPath("$.modified").exists())
                .andExpect(jsonPath("$.created").exists())
                .andExpect(jsonPath("$.registrationUrl").value(SIGNUP_URL + "/api/v1/signup?token=" + token));
    }


    @Test
    @DisplayName("The created signup URI of an participant without a token is null")
    void testNoRegistrationUri() throws Exception {
        when(participantService.getParticipant(1L, 1)).thenAnswer(invocationOnMock ->
                new Participant()
                        .setStudyId(invocationOnMock.getArgument(0, Long.class))
                        .setParticipantId(invocationOnMock.getArgument(1, Integer.class))
                        .setStudyGroupId(1)
                        .setStatus(Participant.Status.NEW)
                        .setAlias("person x")
                        .setCreated(Instant.ofEpochMilli(0))
                        .setModified(Instant.ofEpochMilli(0))
                        .setRegistrationToken(null));

        ParticipantDTO participantRequest = new ParticipantDTO()
                .studyId(1L)
                .participantId(1)
                .studyGroupId(1)
                .alias("person x");

        mvc.perform(get("/api/v1/studies/1/participants/1")
                        .content(mapper.writeValueAsString(participantRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studyId").value(participantRequest.getStudyId()))
                .andExpect(jsonPath("$.participantId").value(participantRequest.getParticipantId()))
                .andExpect(jsonPath("$.studyGroupId").value(participantRequest.getStudyGroupId()))
                .andExpect(jsonPath("$.modified").exists())
                .andExpect(jsonPath("$.created").exists())
                .andExpect(jsonPath("$.registrationUrl").isEmpty());
    }
}
