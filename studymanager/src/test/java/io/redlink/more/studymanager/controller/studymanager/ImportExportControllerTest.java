/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.core.properties.ActionProperties;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.properties.TriggerProperties;
import io.redlink.more.studymanager.model.Action;
import io.redlink.more.studymanager.model.Intervention;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.StudyGroup;
import io.redlink.more.studymanager.model.StudyImportExport;
import io.redlink.more.studymanager.model.Trigger;
import io.redlink.more.studymanager.model.scheduler.Event;
import io.redlink.more.studymanager.model.scheduler.RecurrenceRule;
import io.redlink.more.studymanager.repository.DownloadTokenRepository;
import io.redlink.more.studymanager.service.ImportExportService;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest({ImportExportApiV1Controller.class})
@AutoConfigureMockMvc(addFilters = false)
class ImportExportControllerTest {

    @MockitoBean
    ImportExportService importExportService;

    @MockitoBean
    OAuth2AuthenticationService oAuth2AuthenticationService;

    @MockitoBean
    DownloadTokenRepository tokenRepository;

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("Participants should be exported in csv format as a Resource")
    void testExportParticipants() throws Exception {

        String csv = "STUDYID;TITLE;PARTICIPANTID;ALIAS;REGISTRATIONTOKEN;REGISTRATIONURL\n1;Study;1;SomeAlias;SomeToken;http://examle.com/signup";

        when(importExportService.exportParticipants(any(Long.class), any()))
                .thenAnswer(invocationOnMock -> new ByteArrayResource(csv.getBytes(StandardCharsets.UTF_8)));

        MvcResult result = mvc.perform(get("/api/v1/studies/1/export/participants")
                        .contentType("text/csv"))
                .andDo(print())
                .andReturn();
        assertThat(result.getResponse().getContentAsString()).isEqualTo(csv);
    }

    @Test
    @DisplayName("Test import/export of study configuration")
    void testImportExportStudy() throws Exception {
        Study study = new Study()
                .setStudyId(1L)
                .setTitle("testTitle")
                .setPurpose("testPurpose")
                .setParticipantInfo("testInfo")
                .setConsentInfo("testConsent")
                .setStudyState(Study.Status.DRAFT)
                .setPlannedStartDate(LocalDate.now())
                .setPlannedEndDate(LocalDate.now().plus(2, ChronoUnit.MONTHS));
        StudyGroup group = new StudyGroup()
                .setStudyId(study.getStudyId())
                .setStudyGroupId(1)
                .setTitle("test")
                .setPurpose("test")
                .setCreated(Instant.now());
        Observation observation = new Observation()
                .setStudyId(study.getStudyId())
                .setTitle("testTitle")
                .setPurpose("testPurpose")
                .setParticipantInfo("testInfo")
                .setType("testType")
                .setStudyGroupId(group.getStudyGroupId())
                .setProperties(new ObservationProperties())
                .setCreated(Instant.now())
                .setSchedule(new Event());
        Intervention intervention = new Intervention()
                .setInterventionId(1)
                .setStudyId(study.getStudyId())
                .setTitle("some title")
                .setStudyGroupId(group.getStudyGroupId())
                .setSchedule(new Event().setDateEnd(Instant.now()).setDateEnd(Instant.now().plusSeconds(60)))
                .setSchedule(new Event()
                        .setDateStart(Instant.now())
                        .setDateEnd(Instant.now().plus(2, ChronoUnit.HOURS))
                        .setRRule(new RecurrenceRule().setFreq("DAILY").setCount(7)));

        Trigger trigger = new Trigger()
                .setProperties(new TriggerProperties(Map.of("property", "new value")));

        Action action = new Action()
                .setActionId(1)
                .setProperties(new ActionProperties(Map.of("property", "new value")));

        StudyImportExport studyImportExport = new StudyImportExport()
                .setStudy(study)
                .setStudyGroups(List.of(group))
                .setObservations(List.of(observation))
                .setInterventions(List.of(intervention))
                .setTriggers(Map.of(intervention.getInterventionId(), trigger))
                .setActions(Map.of(intervention.getInterventionId(), List.of(action)))
                .setParticipants(new ArrayList<>())
                .setIntegrations(new ArrayList<>());

        when(importExportService.exportStudy(anyLong(), any()))
                .thenAnswer(invocationOnMock -> studyImportExport);
        when(importExportService.importStudy(any(StudyImportExport.class), any()))
                .thenAnswer(invocationOnMock ->
                        invocationOnMock.getArgument(0, StudyImportExport.class)
                                .getStudy()
                                .setStudyId(2L)
                                .setStudyState(Study.Status.DRAFT)
                                .setCreated(Instant.ofEpochMilli(0))
                                .setModified(Instant.ofEpochMilli(0)));


        MvcResult resultExport = mvc.perform(get("/api/v1/studies/1/export/study")
                        .contentType("application/json"))
                .andDo(print())
                .andReturn();


        mvc.perform(
                multipart("/api/v1/studies/import/study")
                        .file("file", resultExport.getResponse().getContentAsByteArray()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(study.getTitle()))
                .andExpect(jsonPath("$.studyId").value(2L))
                .andExpect(jsonPath("$.purpose").value(study.getPurpose()))
                .andExpect(jsonPath("$.consentInfo").value(study.getConsentInfo()))
                .andExpect(jsonPath("$.participantInfo").value(study.getParticipantInfo()))
                .andExpect(jsonPath("$.plannedStart").exists())
                .andExpect(jsonPath("$.plannedEnd").exists())
                .andExpect(jsonPath("$.modified").exists())
                .andExpect(jsonPath("$.created").exists());
    }
}
