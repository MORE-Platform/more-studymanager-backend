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
import io.redlink.more.studymanager.api.v1.model.EndpointTokenDTO;
import io.redlink.more.studymanager.api.v1.model.ObservationDTO;
import io.redlink.more.studymanager.api.v1.model.ObservationScheduleDTO;
import io.redlink.more.studymanager.model.AuthenticatedUser;
import io.redlink.more.studymanager.model.EndpointToken;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.PlatformRole;
import io.redlink.more.studymanager.model.scheduler.Event;
import io.redlink.more.studymanager.service.IntegrationService;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import io.redlink.more.studymanager.service.ObservationService;
import io.redlink.more.studymanager.utils.MapperUtils;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ObservationsApiV1Controller.class})
@AutoConfigureMockMvc(addFilters = false)
class ObservationControllerTest {

    @MockBean
    IntegrationService integrationService;

    @MockBean
    ObservationService observationService;

    @MockBean
    OAuth2AuthenticationService oAuth2AuthenticationService;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

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
    @DisplayName("Create Observation should create and then return the observation with observation id set")
    void testAddObservation() throws Exception {
        when(observationService.addObservation(any(Observation.class)))
                .thenAnswer(invocationOnMock -> new Observation()
                        .setStudyId(((Observation)invocationOnMock.getArgument(0)).getStudyId())
                        .setObservationId(((Observation)invocationOnMock.getArgument(0)).getObservationId())
                        .setTitle(((Observation)invocationOnMock.getArgument(0)).getTitle())
                        .setPurpose(((Observation)invocationOnMock.getArgument(0)).getPurpose())
                        .setParticipantInfo(((Observation)invocationOnMock.getArgument(0)).getParticipantInfo())
                        .setType(((Observation)invocationOnMock.getArgument(0)).getType())
                        .setProperties(((Observation)invocationOnMock.getArgument(0)).getProperties())
                        .setStudyGroupId(((Observation)invocationOnMock.getArgument(0)).getStudyGroupId())
                        .setCreated(Instant.ofEpochMilli(System.currentTimeMillis()))
                        .setModified(Instant.ofEpochMilli(System.currentTimeMillis()))
                        .setHidden(((Observation) invocationOnMock.getArgument(0)).getHidden()));

        ObservationDTO observationRequest = new ObservationDTO()
                .title("observation 1")
                .observationId(1)
                .studyId(1L)
                .purpose("some purpose")
                .participantInfo("info")
                .type("accelerometer")
                .properties(Map.of("name", "value"))
                .studyGroupId(1)
                .hidden(null);

        mvc.perform(post("/api/v1/studies/1/observations")
                        .content(mapper.writeValueAsString(observationRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(observationRequest.getTitle()))
                .andExpect(jsonPath("$.observationId").value(observationRequest.getObservationId()))
                .andExpect(jsonPath("$.type").value(observationRequest.getType()))
                .andExpect(jsonPath("$.properties.name").value("value"))
                .andExpect(jsonPath("$.hidden").value(observationRequest.getHidden()));
    }

    @Test
    @DisplayName("Update observation should return similar values")
    void testUpdateStudy() throws Exception {
        when(observationService.updateObservation(any(Observation.class))).thenAnswer(invocationOnMock -> ((Observation)invocationOnMock.getArgument(0))
                .setTitle("title")
                .setCreated(Instant.ofEpochMilli(0))
                .setModified(Instant.ofEpochMilli(0)));

        ObservationDTO observationRequest = new ObservationDTO()
                .studyId(1L)
                .title("a different title")
                .observationId(1);

        mvc.perform(put("/api/v1/studies/1/observations/1")
                        .content(mapper.writeValueAsString(observationRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("title"))
                .andExpect(jsonPath("$.studyId").value(observationRequest.getStudyId()))
                .andExpect(jsonPath("$.modified").exists())
                .andExpect(jsonPath("$.created").exists());
    }

    @Test
    @DisplayName("Schedule with empty value should not throw error")
    void testEmptySchedule() throws Exception {
        when(observationService.addObservation(any(Observation.class))).thenAnswer(invocationOnMock -> ((Observation)invocationOnMock.getArgument(0))
                .setTitle("title")
                .setSchedule(new Event().setDateEnd(null).setDateStart(null).setRRule(null))
                .setCreated(Instant.ofEpochMilli(0))
                .setModified(Instant.ofEpochMilli(0)));

        ObservationDTO observationRequest = new ObservationDTO()
                .studyId(1L)
                .title("a different title")
                .schedule(MapperUtils.readValue("{\"type\":\"Event\"}", ObservationScheduleDTO.class))
                .observationId(1);

        mvc.perform(post("/api/v1/studies/1/observations")
                        .content(mapper.writeValueAsString(observationRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("title"))
                .andExpect(jsonPath("$.studyId").value(observationRequest.getStudyId()))
                .andExpect(jsonPath("$.schedule").value(MapperUtils.readValue("{\"type\":\"Event\"}", ObservationScheduleDTO.class)))
                .andExpect(jsonPath("$.modified").exists())
                .andExpect(jsonPath("$.created").exists());
    }

    @Test
    @DisplayName("Add token should create and return token with id, label, timestamp and secret set, only if label is valid")
    void testAddToken() throws Exception{
        EndpointTokenDTO token = new EndpointTokenDTO(
                1,
                "testLabel",
                Instant.now(),
                "test");
        when(integrationService.addToken(anyLong(), anyInt(), anyString()))
                .thenAnswer(invocationOnMock -> Optional.of(new EndpointToken(
                        token.getTokenId(),
                        invocationOnMock.getArgument(2),
                        Instant.now(),
                        token.getToken()
                )));
        mvc.perform(post("/api/v1/studies/1/observations/1/tokens")
                        .content(mapper.writeValueAsString(token))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tokenId").value(token.getTokenId()))
                .andExpect(jsonPath("$.tokenLabel").value(token.getTokenLabel()))
                .andExpect(jsonPath("$.token").value(token.getToken()))
                .andExpect(jsonPath("$.created").exists());

        mvc.perform(post("/api/v1/studies/1/observations/1/tokens")
                        .content("")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/studies/1/observations/1/tokens")
                        .content(" ")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get token should return the token with the given token_id")
    void testGetToken() throws Exception {
        EndpointToken token = new EndpointToken(
                1,
                "testLabel",
                Instant.now(),
                "test");

        when(integrationService.getToken(anyLong(), anyInt(), anyInt()))
                .thenAnswer(invocationOnMock -> Optional.of(new EndpointToken(
                        invocationOnMock.getArgument(2),
                        token.tokenLabel(),
                        token.created(),
                        token.token()
                )));

        mvc.perform(get("/api/v1/studies/1/observations/1/tokens/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenId").value(token.tokenId()))
                .andExpect(jsonPath("$.tokenLabel").value(token.tokenLabel()))
                .andExpect(jsonPath("$.token").value(token.token()))
                .andExpect(jsonPath("$.created").exists());
    }

    @Test
    @DisplayName("Get tokens should return all tokens for given observation")
    void testGetTokens() throws Exception {
        EndpointToken token1 = new EndpointToken(
                1,
                "testLabel1",
                Instant.now(),
                "test1"
        );
        EndpointToken token2 = new EndpointToken(
                2,
                "testLabel2",
                Instant.now(),
                "test2"
        );

        when(integrationService.getTokens(anyLong(), anyInt()))
                .thenAnswer(invocationOnMock -> List.of(
                        new EndpointToken(
                                token1.tokenId(),
                                token1.tokenLabel(),
                                token1.created(),
                                token1.token()),
                        new EndpointToken(
                                token2.tokenId(),
                                token2.tokenLabel(),
                                token2.created(),
                                token2.token()
                        )
                ));

        mvc.perform(get("/api/v1/studies/1/observations/1/tokens"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tokenId").value(token1.tokenId()))
                .andExpect(jsonPath("$[0].tokenLabel").value(token1.tokenLabel()))
                .andExpect(jsonPath("$[0].token").value(token1.token()))
                .andExpect(jsonPath("$[0].created").exists())
                .andExpect(jsonPath("$[1].tokenId").value(token2.tokenId()))
                .andExpect(jsonPath("$[1].tokenLabel").value(token2.tokenLabel()))
                .andExpect(jsonPath("$[1].token").value(token2.token()))
                .andExpect(jsonPath("$[1].created").exists());
    }

    @Test
    @DisplayName("Test exceptions")
    void testExceptionsTokens() throws Exception {
        when(integrationService.addToken(anyLong(), anyInt(), anyString()))
                .thenAnswer(invocationOnMock -> Optional.empty());

        when(integrationService.getToken(anyLong(), anyInt(), anyInt()))
                .thenAnswer(invocationOnMock -> Optional.empty());

        mvc.perform(get("/api/v1/studies/1/observations/1/tokens/1"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/v1/studies/1/observations/1/tokens")
                        .content("testLabel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}



