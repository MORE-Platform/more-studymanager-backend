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
import io.redlink.more.studymanager.api.v1.model.ContactDTO;
import io.redlink.more.studymanager.api.v1.model.StudyDTO;
import io.redlink.more.studymanager.model.AuthenticatedUser;
import io.redlink.more.studymanager.model.PlatformRole;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.User;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import io.redlink.more.studymanager.service.StudyService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({StudyApiV1Controller.class})
@AutoConfigureMockMvc(addFilters = false)
class StudyControllerTest {

    @MockitoBean
    StudyService studyService;

    @MockitoBean
    OAuth2AuthenticationService authService;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    private final AuthenticatedUser authUser = new AuthenticatedUser(
            UUID.randomUUID().toString(),
            "More User",
            "more@example.com",
            "The Hospital",
            EnumSet.allOf(PlatformRole.class));

    @Test
    @DisplayName("Create study should create and then return the study with id and status set.")
    void testCreateStudy() throws Exception {
        when(authService.getCurrentUser()).thenReturn(authUser);
        when(studyService.createStudy(any(Study.class), any(User.class))).thenAnswer(invocationOnMock -> new Study()
                .setTitle(((Study) invocationOnMock.getArgument(0)).getTitle())
                .setStudyId(1L)
                .setPlannedStartDate(((Study) invocationOnMock.getArgument(0)).getPlannedStartDate())
                .setPlannedEndDate(((Study) invocationOnMock.getArgument(0)).getPlannedEndDate())
                .setStudyState(Study.Status.DRAFT)
                .setFinishText(((Study) invocationOnMock.getArgument(0)).getFinishText())
                .setCreated(Instant.ofEpochMilli(System.currentTimeMillis()))
                .setModified(Instant.ofEpochMilli(System.currentTimeMillis()))
                .setContact(((Study) invocationOnMock.getArgument(0)).getContact()));

        StudyDTO studyRequest = new StudyDTO()
                .title("Some title")
                .finishText("servus")
                .plannedStart(LocalDate.now())
                .plannedEnd(LocalDate.now())
                .contact(new ContactDTO().person("test").email("test"));

        mvc.perform(post("/api/v1/studies")
                        .content(mapper.writeValueAsString(studyRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(studyRequest.getTitle()))
                .andExpect(jsonPath("$.studyId").value(1L))
                .andExpect(jsonPath("$.status").value("draft"))
                .andExpect(jsonPath("$.finishText").value("servus"))
                .andExpect(jsonPath("$.contact").exists());
    }

    @Test
    @DisplayName("Update study should return similar values")
    void testUpdateStudy() throws Exception {
        when(studyService.updateStudy(any(Study.class), any())).thenAnswer(invocationOnMock ->
                Optional.of(
                        invocationOnMock.getArgument(0, Study.class)
                                .setStudyState(Study.Status.DRAFT)
                                .setCreated(Instant.ofEpochMilli(0))
                                .setModified(Instant.ofEpochMilli(0))
                                .setContact(((Study) invocationOnMock.getArgument(0)).getContact())
                ));

        StudyDTO studyRequest = new StudyDTO()
                .studyId(1L)
                .title("t")
                .purpose("p")
                .consentInfo("ci")
                .participantInfo("pi")
                .plannedStart(LocalDate.now())
                .plannedEnd(LocalDate.now())
                .contact(new ContactDTO().person("test").email("test"));

        mvc.perform(put("/api/v1/studies/1")
                        .content(mapper.writeValueAsString(studyRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(studyRequest.getTitle()))
                .andExpect(jsonPath("$.studyId").value(studyRequest.getStudyId()))
                .andExpect(jsonPath("$.purpose").value(studyRequest.getPurpose()))
                .andExpect(jsonPath("$.consentInfo").value(studyRequest.getConsentInfo()))
                .andExpect(jsonPath("$.participantInfo").value(studyRequest.getParticipantInfo()))
                .andExpect(jsonPath("$.plannedStart").exists())
                .andExpect(jsonPath("$.plannedEnd").exists())
                .andExpect(jsonPath("$.modified").exists())
                .andExpect(jsonPath("$.created").exists())
                .andExpect(jsonPath("$.contact").exists())
        ;
    }
}
