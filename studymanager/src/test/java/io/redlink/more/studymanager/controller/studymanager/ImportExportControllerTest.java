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
import io.redlink.more.studymanager.model.AuthenticatedUser;
import io.redlink.more.studymanager.model.Intervention;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.ObservationGroup;
import io.redlink.more.studymanager.model.PlatformRole;
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
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
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
    @DisplayName("Participants should be exported in csv format as a Resource")
    void testExportParticipants() throws Exception {

        String csv = "STUDYID;TITLE;PARTICIPANTID;ALIAS;REGISTRATIONTOKEN;REGISTRATIONURL\n1;Study;1;SomeAlias;SomeToken;http://examle.com/signup";

        when(importExportService.exportParticipants(any(Long.class), any()))
                .thenAnswer(invocationOnMock -> new ByteArrayResource(csv.getBytes(StandardCharsets.UTF_8)));

        MvcResult result = mvc.perform(get("/api/v1/studies/1/export/participants")
                        .accept("text/csv")
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
                .setPlannedStartDate(LocalDate.parse("2026-02-19"))
                .setPlannedEndDate(LocalDate.parse("2026-04-19"))
                .setCreated(Instant.now())
                .setModified(Instant.now());
        StudyGroup group = new StudyGroup()
                .setStudyId(study.getStudyId())
                .setStudyGroupId(1)
                .setTitle("test")
                .setPurpose("test")
                .setCreated(Instant.now())
                .setModified(Instant.now());
        ObservationGroup observationGroup1 = new ObservationGroup()
                .setStudyId(study.getStudyId())
                .setObservationGroupId(1)
                .setTitle("ObservationGroup 1")
                .setPurpose("test 1")
                .setCreated(Instant.now())
                .setModified(Instant.now());
        ObservationGroup observationGroup2 = new ObservationGroup()
                .setStudyId(study.getStudyId())
                .setObservationGroupId(2)
                .setTitle("ObservationGroup 2")
                .setPurpose("test 2")
                .setCreated(Instant.now())
                .setModified(Instant.now());
        Observation observation = new Observation()
                .setStudyId(study.getStudyId())
                .setTitle("testTitle")
                .setPurpose("testPurpose")
                .setParticipantInfo("testInfo")
                .setType("testType")
                .setStudyGroupId(group.getStudyGroupId())
                .setProperties(new ObservationProperties())
                .setCreated(Instant.now())
                .setSchedule(new Event())
                .setObservationGroupIds(Set.of(1))
                .setCreated(Instant.now())
                .setModified(Instant.now());
        Intervention intervention = new Intervention()
                .setInterventionId(1)
                .setStudyId(study.getStudyId())
                .setTitle("some title")
                .setStudyGroupId(group.getStudyGroupId())
                .setObservationGroupIds(Set.of(2))
                .setSchedule(new Event().setDateEnd(Instant.now()).setDateEnd(Instant.now().plusSeconds(60)))
                .setSchedule(new Event()
                        .setDateStart(Instant.parse("2025-11-12T10:00:00Z"))
                        .setDateEnd(Instant.parse("2025-11-12T12:00:00Z"))
                        .setRRule(new RecurrenceRule().setFreq("DAILY").setCount(7)))
                .setCreated(Instant.now())
                .setModified(Instant.now());

        Trigger trigger = new Trigger()
                .setProperties(new TriggerProperties(Map.of("property", "new value")))
                .setCreated(Instant.now())
                .setModified(Instant.now());

        Action action = new Action()
                .setActionId(1)
                .setProperties(new ActionProperties(Map.of("property", "new value")))
                .setCreated(Instant.now())
                .setModified(Instant.now());

        StudyImportExport studyImportExport = new StudyImportExport()
                .setStudy(study)
                .setStudyGroups(List.of(group))
                .setObservationGroups(List.of(observationGroup1, observationGroup2))
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
                .andExpect(jsonPath("$.study").isMap())
                .andExpect(jsonPath("$.study.studyId").value(1))
                .andExpect(jsonPath("$.study.title").value("testTitle"))
                .andExpect(jsonPath("$.study.purpose").value("testPurpose"))
                .andExpect(jsonPath("$.study.participantInfo").value("testInfo"))
                .andExpect(jsonPath("$.study.consentInfo").value("testConsent"))
                .andExpect(jsonPath("$.study.duration").isEmpty())
                .andExpect(jsonPath("$.study.finishText").isEmpty())
                .andExpect(jsonPath("$.study.status").value("draft"))
                .andExpect(jsonPath("$.study.start").isEmpty())
                .andExpect(jsonPath("$.study.end").isEmpty())
                .andExpect(jsonPath("$.study.plannedStart").value("2026-02-19"))
                .andExpect(jsonPath("$.study.plannedEnd").value("2026-04-19"))
                .andExpect(jsonPath("$.study.created").exists())
                .andExpect(jsonPath("$.study.modified").exists())
                .andExpect(jsonPath("$.study.userRoles").isEmpty())
                .andExpect(jsonPath("$.study.contact").isMap())
                .andExpect(jsonPath("$.study.contact.institute").isEmpty())
                .andExpect(jsonPath("$.study.contact.person").isEmpty())
                .andExpect(jsonPath("$.study.contact.email").isEmpty())
                .andExpect(jsonPath("$.study.contact.phoneNumber").isEmpty())
                .andExpect(jsonPath("$.studyGroups").isArray())
                .andExpect(jsonPath("$.studyGroups.length()").value(1))
                .andExpect(jsonPath("$.studyGroups[0].studyId").value(1))
                .andExpect(jsonPath("$.studyGroups[0].studyGroupId").value(1))
                .andExpect(jsonPath("$.studyGroups[0].title").value("test"))
                .andExpect(jsonPath("$.studyGroups[0].purpose").value("test"))
                .andExpect(jsonPath("$.studyGroups[0].duration").isEmpty())
                .andExpect(jsonPath("$.studyGroups[0].numberOfParticipants").isEmpty())
                .andExpect(jsonPath("$.studyGroups[0].created").exists())
                .andExpect(jsonPath("$.studyGroups[0].modified").exists())
                .andExpect(jsonPath("$.observationGroups").isArray())
                .andExpect(jsonPath("$.observationGroups.length()").value(2))
                .andExpect(jsonPath("$.observationGroups[0].studyId").value(1))
                .andExpect(jsonPath("$.observationGroups[0].observationGroupId").value(1))
                .andExpect(jsonPath("$.observationGroups[0].title").value("ObservationGroup 1"))
                .andExpect(jsonPath("$.observationGroups[0].purpose").value("test 1"))
                .andExpect(jsonPath("$.observationGroups[0].numberOfParticipants").isEmpty())
                .andExpect(jsonPath("$.observationGroups[0].numberOfObservations").isEmpty())
                .andExpect(jsonPath("$.observationGroups[0].numberOfInterventions").isEmpty())
                .andExpect(jsonPath("$.observationGroups[0].created").exists())
                .andExpect(jsonPath("$.observationGroups[0].modified").exists())
                .andExpect(jsonPath("$.observationGroups[1].studyId").value(1))
                .andExpect(jsonPath("$.observationGroups[1].observationGroupId").value(2))
                .andExpect(jsonPath("$.observationGroups[1].title").value("ObservationGroup 2"))
                .andExpect(jsonPath("$.observationGroups[1].purpose").value("test 2"))
                .andExpect(jsonPath("$.observationGroups[1].numberOfParticipants").isEmpty())
                .andExpect(jsonPath("$.observationGroups[1].numberOfObservations").isEmpty())
                .andExpect(jsonPath("$.observationGroups[1].numberOfInterventions").isEmpty())
                .andExpect(jsonPath("$.observationGroups[1].created").exists())
                .andExpect(jsonPath("$.observationGroups[1].modified").exists())
                .andExpect(jsonPath("$.observations").isArray())
                .andExpect(jsonPath("$.observations.length()").value(1))
                .andExpect(jsonPath("$.observations[0].studyId").value(1))
                .andExpect(jsonPath("$.observations[0].observationId").isEmpty())
                .andExpect(jsonPath("$.observations[0].studyGroupId").value(1))
                .andExpect(jsonPath("$.observations[0].title").value("testTitle"))
                .andExpect(jsonPath("$.observations[0].purpose").value("testPurpose"))
                .andExpect(jsonPath("$.observations[0].participantInfo").value("testInfo"))
                .andExpect(jsonPath("$.observations[0].type").value("testType"))
                .andExpect(jsonPath("$.observations[0].properties").isEmpty())
                .andExpect(jsonPath("$.observations[0].schedule").isMap())
                .andExpect(jsonPath("$.observations[0].schedule.type").value("Event"))
                .andExpect(jsonPath("$.observations[0].schedule.dtstart").isEmpty())
                .andExpect(jsonPath("$.observations[0].schedule.dtend").isEmpty())
                .andExpect(jsonPath("$.observations[0].schedule.rrule").isEmpty())
                .andExpect(jsonPath("$.observations[0].schedule.random").isEmpty())
                .andExpect(jsonPath("$.observations[0].created").exists())
                .andExpect(jsonPath("$.observations[0].modified").exists())
                .andExpect(jsonPath("$.observations[0].hidden").isEmpty())
                .andExpect(jsonPath("$.observations[0].noSchedule").value(false))
                .andExpect(jsonPath("$.observations[0].reminder").value(false))
                .andExpect(jsonPath("$.observations[0].observationGroupIds").isArray())
                .andExpect(jsonPath("$.observations[0].observationGroupIds.length()").value(1))
                .andExpect(jsonPath("$.observations[0].observationGroupIds[0]").value(1))
                .andExpect(jsonPath("$.interventions").isArray())
                .andExpect(jsonPath("$.interventions.length()").value(1))
                .andExpect(jsonPath("$.interventions[0].studyId").value(1))
                .andExpect(jsonPath("$.interventions[0].interventionId").value(1))
                .andExpect(jsonPath("$.interventions[0].studyGroupId").value(1))
                .andExpect(jsonPath("$.interventions[0].title").value("some title"))
                .andExpect(jsonPath("$.interventions[0].purpose").isEmpty())
                .andExpect(jsonPath("$.interventions[0].schedule").isMap())
                .andExpect(jsonPath("$.interventions[0].schedule.type").value("Event"))
                .andExpect(jsonPath("$.interventions[0].schedule.dtstart").value("2025-11-12T10:00:00Z"))
                .andExpect(jsonPath("$.interventions[0].schedule.dtend").value("2025-11-12T12:00:00Z"))
                .andExpect(jsonPath("$.interventions[0].schedule.rrule").isMap())
                .andExpect(jsonPath("$.interventions[0].schedule.rrule.freq").value("DAILY"))
                .andExpect(jsonPath("$.interventions[0].schedule.rrule.until").isEmpty())
                .andExpect(jsonPath("$.interventions[0].schedule.rrule.count").value(7))
                .andExpect(jsonPath("$.interventions[0].schedule.rrule.interval").isEmpty())
                .andExpect(jsonPath("$.interventions[0].schedule.rrule.byday").isEmpty())
                .andExpect(jsonPath("$.interventions[0].schedule.rrule.bymonth").isEmpty())
                .andExpect(jsonPath("$.interventions[0].schedule.rrule.bymonthday").isEmpty())
                .andExpect(jsonPath("$.interventions[0].schedule.rrule.bysetpos").isEmpty())
                .andExpect(jsonPath("$.interventions[0].schedule.random").isEmpty())
                .andExpect(jsonPath("$.interventions[0].trigger").isMap())
                .andExpect(jsonPath("$.interventions[0].trigger.type").isEmpty())
                .andExpect(jsonPath("$.interventions[0].trigger.properties").isMap())
                .andExpect(jsonPath("$.interventions[0].trigger.properties.property").value("new value"))
                .andExpect(jsonPath("$.interventions[0].trigger.created").exists())
                .andExpect(jsonPath("$.interventions[0].trigger.modified").exists())
                .andExpect(jsonPath("$.interventions[0].actions").isArray())
                .andExpect(jsonPath("$.interventions[0].actions.length()").value(1))
                .andExpect(jsonPath("$.interventions[0].actions[0].actionId").value(1))
                .andExpect(jsonPath("$.interventions[0].actions[0].type").isEmpty())
                .andExpect(jsonPath("$.interventions[0].actions[0].properties").isMap())
                .andExpect(jsonPath("$.interventions[0].actions[0].properties.property").value("new value"))
                .andExpect(jsonPath("$.interventions[0].actions[0].created").exists())
                .andExpect(jsonPath("$.interventions[0].actions[0].modified").exists())
                .andExpect(jsonPath("$.interventions[0].observationGroupIds").isArray())
                .andExpect(jsonPath("$.interventions[0].observationGroupIds.length()").value(1))
                .andExpect(jsonPath("$.interventions[0].observationGroupIds[0]").value(2))
                .andExpect(jsonPath("$.interventions[0].created").exists())
                .andExpect(jsonPath("$.interventions[0].modified").exists())
                .andExpect(jsonPath("$.participants").isArray())
                .andExpect(jsonPath("$.participants.length()").value(0))
                .andExpect(jsonPath("$.integrations").isArray())
                .andExpect(jsonPath("$.integrations.length()").value(0))
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
