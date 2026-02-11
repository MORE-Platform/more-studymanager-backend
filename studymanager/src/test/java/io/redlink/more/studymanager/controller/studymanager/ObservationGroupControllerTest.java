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
import io.redlink.more.studymanager.api.v1.model.ObservationGroupDTO;
import io.redlink.more.studymanager.model.*;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import io.redlink.more.studymanager.service.ObservationGroupService;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ObservationGroupApiV1Controller.class})
@AutoConfigureMockMvc(addFilters = false)
class ObservationGroupControllerTest {


    @MockitoBean
    ObservationGroupService observationGroupService;

    @MockitoBean
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
    @DisplayName("Create ObservationGroup")
    void testCreateObservationGroup() throws Exception {
        when(observationGroupService.createObservationGroup(any(ObservationGroup.class)))
                .thenAnswer(invocationOnMock -> new ObservationGroup()
                        .setStudyId(((ObservationGroup)invocationOnMock.getArgument(0)).getStudyId())
                        .setObservationGroupId(1)
                        .setTitle(((ObservationGroup)invocationOnMock.getArgument(0)).getTitle())
                        .setPurpose(((ObservationGroup)invocationOnMock.getArgument(0)).getPurpose())
                        .setCreated(Instant.ofEpochMilli(System.currentTimeMillis()))
                        .setModified(Instant.ofEpochMilli(System.currentTimeMillis())));

        ObservationGroupDTO observationGroupRequestBody = new ObservationGroupDTO()
                .studyId(13L) //expected to be overridden by the controller to the id parts as path
                .title("observation group 1")
                .purpose("for testing only");

        mvc.perform(post("/api/v1/studies/1/observationGroups")
                        .content(mapper.writeValueAsString(observationGroupRequestBody))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studyId").value(1L))
                .andExpect(jsonPath("$.observationGroupId").value(1))
                .andExpect(jsonPath("$.title").value(observationGroupRequestBody.getTitle()))
                .andExpect(jsonPath("$.purpose").value(observationGroupRequestBody.getPurpose()))
                .andExpect(jsonPath("$.created").exists())
                .andExpect(jsonPath("$.modified").exists());
    }

    @Test
    @DisplayName("Update observation group")
    void testUpdateObservationGroup() throws Exception {
        when(observationGroupService.updateObservationGroup(any(ObservationGroup.class))).thenAnswer(invocationOnMock ->
                new ObservationGroup()
                        .setStudyId(((ObservationGroup)invocationOnMock.getArgument(0)).getStudyId())
                        .setObservationGroupId(((ObservationGroup)invocationOnMock.getArgument(0)).getObservationGroupId())
                        .setTitle(((ObservationGroup)invocationOnMock.getArgument(0)).getTitle())
                        .setPurpose(((ObservationGroup)invocationOnMock.getArgument(0)).getPurpose())
                        .setCreated(Instant.now().minusSeconds(100))
                        .setModified(Instant.now().minusSeconds(50)));


        ObservationGroupDTO observationRequest = new ObservationGroupDTO()
                .studyId(13L) //need to be ignored as this is specified via path parameters
                .observationGroupId(13) //need to be ignored as this is specified via path parameters
                .title("the updated title")
                .purpose("the purpose of life")
                .created(Instant.now().minusSeconds(100));

        mvc.perform(put("/api/v1/studies/1/observationGroups/1")
                        .content(mapper.writeValueAsString(observationRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studyId").value(1)) //as in the path
                .andExpect(jsonPath("$.observationGroupId").value(1)) //as in the path
                .andExpect(jsonPath("$.title").value(observationRequest.getTitle()))
                .andExpect(jsonPath("$.purpose").value(observationRequest.getPurpose()))
                .andExpect(jsonPath("$.modified").exists())
                .andExpect(jsonPath("$.created").exists());
    }

    @Test
    @DisplayName("List all observation groups for a study")
    void testListObservationGroup() throws Exception {
        when(observationGroupService.listObservationGroups(any(Long.class))).thenAnswer(invocationOnMock -> {
            Long studyId = ((Long) invocationOnMock.getArgument(0));
            Assertions.assertThat(studyId).isEqualTo(1L);
            return List.of(
                    new ObservationGroup()
                            .setStudyId(studyId)
                            .setObservationGroupId(1)
                            .setTitle("Observation group 1")
                            .setPurpose("purpose 1")
                            .setCreated(Instant.now().minusSeconds(100))
                            .setModified(Instant.now().minusSeconds(50)),
                    new ObservationGroup()
                            .setStudyId(studyId)
                            .setObservationGroupId(2)
                            .setTitle("Observation group 2")
                            .setPurpose("purpose 2")
                            .setCreated(Instant.now().minusSeconds(25))
                            .setModified(Instant.now().minusSeconds(12)),
                    new ObservationGroup()
                            .setStudyId(studyId)
                            .setObservationGroupId(3)
                            .setTitle("Observation group 3")
                            .setPurpose("purpose 3")
                            .setCreated(Instant.now().minusSeconds(6))
                            .setModified(Instant.now().minusSeconds(3))
                    );
        });

        when(observationGroupService.countObservationsInGroup(anyLong(), anyInt())).thenAnswer(invocationOnMock -> {
            Long studyId = ((Long) invocationOnMock.getArgument(0));
            Integer groupId = ((Integer) invocationOnMock.getArgument(1));
            return (studyId.intValue() + groupId) * 2;
        });
        when(observationGroupService.countInterventionsInGroup(anyLong(), anyInt())).thenAnswer(invocationOnMock -> {
            Long studyId = ((Long) invocationOnMock.getArgument(0));
            Integer groupId = ((Integer) invocationOnMock.getArgument(1));
            return (studyId.intValue() + groupId) * 3;
        });
        when(observationGroupService.countParticipantsInGroup(anyLong(), anyInt())).thenAnswer(invocationOnMock -> {
            Long studyId = ((Long) invocationOnMock.getArgument(0));
            Integer groupId = ((Integer) invocationOnMock.getArgument(1));
            return (studyId.intValue() + groupId) * 4;
        });

        mvc.perform(get("/api/v1/studies/1/observationGroups"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", Matchers.hasSize(3)))
                .andExpect(jsonPath("$[0].studyId").value(1))
                .andExpect(jsonPath("$[0].observationGroupId").value(1))
                .andExpect(jsonPath("$[0].title").value("Observation group 1"))
                .andExpect(jsonPath("$[0].purpose").value("purpose 1"))
                .andExpect(jsonPath("$[0].numberOfObservations").value(4))
                .andExpect(jsonPath("$[0].numberOfInterventions").value(6))
                .andExpect(jsonPath("$[0].numberOfParticipants").value(8))
                .andExpect(jsonPath("$[0].created").exists())
                .andExpect(jsonPath("$[0].modified").exists())
                .andExpect(jsonPath("$[1].studyId").value(1))
                .andExpect(jsonPath("$[1].observationGroupId").value(2))
                .andExpect(jsonPath("$[1].numberOfObservations").value(6))
                .andExpect(jsonPath("$[1].numberOfInterventions").value(9))
                .andExpect(jsonPath("$[1].numberOfParticipants").value(12))
                .andExpect(jsonPath("$[2].studyId").value(1))
                .andExpect(jsonPath("$[2].observationGroupId").value(3))
                .andExpect(jsonPath("$[2].numberOfObservations").value(8))
                .andExpect(jsonPath("$[2].numberOfInterventions").value(12))
                .andExpect(jsonPath("$[2].numberOfParticipants").value(16));
    }

    @Test
    @DisplayName("Get Observation Group")
    void testGetObservationGroup() throws Exception{
        when(observationGroupService.getObservationGroup(any(Long.class),any(Integer.class))).thenAnswer(invocationOnMock -> {
            Long studyId = ((Long) invocationOnMock.getArgument(0));
            Integer observationGroup = ((Integer) invocationOnMock.getArgument(1));
            return new ObservationGroup()
                    .setStudyId(studyId)
                    .setObservationGroupId(observationGroup)
                    .setTitle("Observation group " + observationGroup + " of study " + studyId)
                    .setPurpose("purpose of observation group " + observationGroup + " of study " + studyId)
                    .setCreated(Instant.now().minusSeconds(100))
                    .setModified(Instant.now().minusSeconds(50));
        });

        mvc.perform(get("/api/v1/studies/3/observationGroups/23"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studyId").value(3L))
                .andExpect(jsonPath("$.observationGroupId").value(23))
                .andExpect(jsonPath("$.title").value("Observation group 23 of study 3"))
                .andExpect(jsonPath("$.purpose").value("purpose of observation group 23 of study 3"))
                .andExpect(jsonPath("$.created").exists())
                .andExpect(jsonPath("$.modified").exists());
    }
    @Test
    @DisplayName("Delete Observation Group")
    void testDeleteObservationGroup() throws Exception{
        Mockito.doNothing().when(observationGroupService).deleteObservationGroup(any(Long.class),any(Integer.class));

        mvc.perform(delete("/api/v1/studies/3/observationGroups/23"))
                .andDo(print())
                .andExpect(status().isNoContent());

        Mockito.verify(observationGroupService, times(1)).deleteObservationGroup(eq(3L), eq(23));
    }
}



