/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.core.properties.ActionProperties;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.properties.TriggerProperties;
import io.redlink.more.studymanager.model.*;
import io.redlink.more.studymanager.model.scheduler.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImportExportServiceTest {

    @Spy
    private ParticipantService participantService = mock(ParticipantService.class);

    @Spy
    private IntegrationService integrationService = mock(IntegrationService.class);

    @Mock
    private StudyService studyService;

    @Mock
    private StudyStateService studyStateService;

    @Spy
    private ObservationService observationService = mock(ObservationService.class);

    @Spy
    private InterventionService interventionService = mock(InterventionService.class);

    @Spy
    private StudyGroupService studyGroupService = mock(StudyGroupService.class);

    @Spy
    private ObservationGroupService observationGroupService = mock(ObservationGroupService.class);
    
    @Spy
    private MilestoneService milestoneService = mock(MilestoneService.class);

    @Spy
    private ParticipantMilestoneService participantMilestoneService = mock(ParticipantMilestoneService.class);

    @InjectMocks
    ImportExportService importExportService;

    @Captor
    private ArgumentCaptor<Participant> participantsCaptor;

    @Captor
    private ArgumentCaptor<Observation> observationCaptor;

    @Captor
    private ArgumentCaptor<Intervention> interventionCaptor;

    @Captor
    private ArgumentCaptor<Trigger> triggerCaptor;

    @Captor
    private ArgumentCaptor<List<Action>> actionCaptor;

    @Captor
    private ArgumentCaptor<Long> idLongCaptor;

    @Captor
    private ArgumentCaptor<Integer> idIntegerCaptor;

    @Captor
    private ArgumentCaptor<String> aliasCaptor;

    @Captor
    private ArgumentCaptor<Milestone> milestoneCaptor;

    private final AuthenticatedUser currentUser = new AuthenticatedUser(
            UUID.randomUUID().toString(),
            "Test User", "test@example.com", "Test Inc.",
            EnumSet.allOf(PlatformRole.class)
    );

    @Test
    @DisplayName("CSV should be imported line by line as Participant (header line skipped)")
    void testImportParticipants() throws FileNotFoundException {
        File file = ResourceUtils.getFile("classpath:import/participants-groups-test2.csv");

        importExportService.importParticipants(1L, new FileInputStream(file));

        verify(participantService, times(4)).createParticipant(participantsCaptor.capture());
        assertThat(participantsCaptor.getValue().getAlias()).isEqualTo("more than 2 words");
    }

    @Test
    @DisplayName("Study configuration should be imported and set id's correctly")
    void testImportStudy() {
        Long studyId = 1L;
        StudyImportExport studyImport = new StudyImportExport()
                .setStudy(new Study()
                        .setTitle("title")
                        .setPurpose("purpose")
                        .setParticipantInfo("info")
                        .setConsentInfo("consent")
                        .setPlannedStartDate(LocalDate.now())
                        .setPlannedEndDate(LocalDate.now()))
                .setObservations(List.of(
                        new Observation()
                                .setObservationId(1)
                                .setTitle("observation Title")
                                .setPurpose("observation purpose")
                                .setParticipantInfo("observation info")
                                .setType("gps-mobile-observation")
                                .setStudyGroupId(3)
                                .setProperties(new ObservationProperties())
                                .setSchedule(new Event())
                                .setObservationGroupIds(Set.of(1)),
                        new Observation()
                                .setObservationId(3)
                                .setTitle("observation Title")
                                .setPurpose("observation purpose")
                                .setParticipantInfo("observation info")
                                .setType("gps-mobile-observation")
                                .setStudyGroupId(null)
                                .setProperties(new ObservationProperties())
                                .setSchedule(new Event())
                                .setObservationGroupIds(Set.of(2)),
                        new Observation()
                                .setObservationId(4)
                                .setTitle("observation Title")
                                .setPurpose("observation purpose")
                                .setParticipantInfo("observation info")
                                .setType("gps-mobile-observation")
                                .setStudyGroupId(null)
                                .setProperties(new ObservationProperties())
                                .setSchedule(new Event())
                                .setObservationGroupIds(Set.of(1, 2))))
                .setStudyGroups(List.of(
                        new StudyGroup()
                                .setStudyGroupId(2)
                                .setTitle("group title")
                                .setPurpose("group purpose"),
                        new StudyGroup()
                                .setStudyGroupId(3)
                                .setTitle("group title2")
                                .setPurpose("group purpose2")))
                .setObservationGroups(List.of(
                        new ObservationGroup()
                                .setObservationGroupId(1)
                                .setTitle("observation group title 1")
                                .setPurpose("observation group purpose 1"),
                        new ObservationGroup()
                                .setObservationGroupId(2)
                                .setTitle("observation group title 2")
                                .setPurpose("observation group purpose 2")))
                .setInterventions(List.of(
                        new Intervention()
                                .setInterventionId(2)
                                .setTitle("intervention title")
                                .setPurpose("intervention purpose")
                                .setStudyGroupId(2)
                                .setSchedule(new Event())
                                .setObservationGroupIds(Set.of(1)),
                        new Intervention()
                                .setInterventionId(3)
                                .setTitle("intervention title")
                                .setPurpose("intervention purpose")
                                .setStudyGroupId(3)
                                .setSchedule(new Event())
                                .setObservationGroupIds(Set.of(2)),
                        new Intervention()
                                .setInterventionId(4)
                                .setTitle("intervention title")
                                .setPurpose("intervention purpose")
                                .setStudyGroupId(3)
                                .setSchedule(new Event())
                                .setObservationGroupIds(Set.of(1, 2))))
                .setTriggers(Map.of(3, new Trigger()
                        .setType("sth")
                        .setProperties(new TriggerProperties())))
                .setActions(Map.of(2, List.of(new Action()
                        .setType("sth")
                        .setProperties(new ActionProperties()))))
                .setMilestones(List.of(
                        new Milestone().setMilestoneId(1).setName("Milestone 1").setOrderIndex(0),
                        new Milestone().setMilestoneId(2).setName("Milestone 2").setOrderIndex(1)))
                .setParticipants(List.of(
                        new StudyImportExport.ParticipantInfo(0, null),
                        new StudyImportExport.ParticipantInfo(0, Set.of(1)),
                        new StudyImportExport.ParticipantInfo(0, Set.of(1, 2)),
                        new StudyImportExport.ParticipantInfo(2, Set.of()),
                        new StudyImportExport.ParticipantInfo(2, Set.of(2)),
                        new StudyImportExport.ParticipantInfo(2, Set.of(1, 2)),
                        new StudyImportExport.ParticipantInfo(4, Set.of(1)),
                        new StudyImportExport.ParticipantInfo(4, Set.of(2),
                                List.of(new ParticipantMilestoneInfo(2, Instant.parse("2026-01-15T10:00:00Z"))))
                ))
                .setIntegrations(List.of(
                        new IntegrationInfo("Integration 1", 1),
                        new IntegrationInfo("Integration 2", 3)
                ));

        when(studyService.createStudy(any(), any()))
                .thenAnswer(invocationOnMock ->
                        ((Study) invocationOnMock.getArgument(0)).setStudyId(studyId));
        when(observationService.importObservation(any(), any()))
                .thenAnswer(invocationOnMock ->
                        ((Observation) invocationOnMock.getArgument(1)).setStudyId(studyId));
        when(interventionService.importIntervention(any(), any(), any(), any()))
                .thenAnswer(invocationOnMock ->
                        ((Intervention) invocationOnMock.getArgument(1)).setStudyId(studyId));
        when(participantService.createParticipant(any()))
                .thenAnswer(invocationOnMock ->
                        ((Participant) invocationOnMock.getArgument(0)).setParticipantId(42));

        importExportService.importStudy(studyImport, currentUser);

        verify(milestoneService, times(2)).importMilestone(idLongCaptor.capture(), milestoneCaptor.capture());
        assertThat(milestoneCaptor.getAllValues().get(0).getMilestoneId()).isEqualTo(1);
        assertThat(milestoneCaptor.getAllValues().get(1).getMilestoneId()).isEqualTo(2);

        verify(participantMilestoneService).createParticipantMilestone(
                eq(studyId), eq(42), eq(2), eq(Instant.parse("2026-01-15T10:00:00Z")));

        ArgumentCaptor<StudyGroup> studyGroupCaptor = ArgumentCaptor.forClass(StudyGroup.class);
        verify(studyGroupService, times(2)).importStudyGroup(idLongCaptor.capture(), studyGroupCaptor.capture());
        assertThat(studyGroupCaptor.getAllValues()).hasSize(2);
        assertThat(studyGroupCaptor.getAllValues().get(0).getStudyGroupId()).isEqualTo(2);
        assertThat(studyGroupCaptor.getAllValues().get(1).getStudyGroupId()).isEqualTo(3);

        ArgumentCaptor<ObservationGroup> observationGroupCaptor = ArgumentCaptor.forClass(ObservationGroup.class);
        verify(observationGroupService, times(2)).importObservationGroup(idLongCaptor.capture(), observationGroupCaptor.capture());
        assertThat(studyGroupCaptor.getAllValues()).hasSize(2);
        assertThat(studyGroupCaptor.getAllValues().get(0).getStudyGroupId()).isEqualTo(2);
        assertThat(studyGroupCaptor.getAllValues().get(1).getStudyGroupId()).isEqualTo(3);


        verify(observationService, times(3)).importObservation(idLongCaptor.capture(), observationCaptor.capture());
        verify(interventionService, times(3)).importIntervention(idLongCaptor.capture(), interventionCaptor.capture(), triggerCaptor.capture(), actionCaptor.capture());
        verify(participantService, times(8)).createParticipant(participantsCaptor.capture());
        verify(integrationService, times(2)).addToken(idLongCaptor.capture(), idIntegerCaptor.capture(), aliasCaptor.capture());

        assertThat(observationCaptor.getAllValues().get(0).getObservationId()).isEqualTo(1);
        assertThat(observationCaptor.getAllValues().get(0).getStudyGroupId()).isEqualTo(3);
        assertThat(observationCaptor.getAllValues().get(0).getObservationGroupIds()).containsExactlyInAnyOrder(1);
        assertThat(observationCaptor.getAllValues().get(1).getObservationId()).isEqualTo(3);
        assertThat(observationCaptor.getAllValues().get(1).getStudyGroupId()).isNull();
        assertThat(observationCaptor.getAllValues().get(1).getObservationGroupIds()).containsExactlyInAnyOrder(2);
        assertThat(observationCaptor.getAllValues().get(2).getObservationId()).isEqualTo(4);
        assertThat(observationCaptor.getAllValues().get(2).getStudyGroupId()).isNull();
        assertThat(observationCaptor.getAllValues().get(2).getObservationGroupIds()).containsExactlyInAnyOrder(1, 2);

        assertThat(interventionCaptor.getAllValues().get(0).getInterventionId()).isEqualTo(2);
        assertThat(interventionCaptor.getAllValues().get(0).getStudyGroupId()).isEqualTo(2);
        assertThat(interventionCaptor.getAllValues().get(0).getObservationGroupIds()).containsExactlyInAnyOrder(1);
        assertThat(interventionCaptor.getAllValues().get(1).getInterventionId()).isEqualTo(3);
        assertThat(interventionCaptor.getAllValues().get(1).getStudyGroupId()).isEqualTo(3);
        assertThat(interventionCaptor.getAllValues().get(1).getObservationGroupIds()).containsExactlyInAnyOrder(2);
        assertThat(interventionCaptor.getAllValues().get(2).getInterventionId()).isEqualTo(4);
        assertThat(interventionCaptor.getAllValues().get(2).getStudyGroupId()).isEqualTo(3);
        assertThat(interventionCaptor.getAllValues().get(2).getObservationGroupIds()).containsExactlyInAnyOrder(1, 2);

        assertThat(idLongCaptor.getAllValues()).allMatch(Predicate.isEqual(1L));

        assertThat(participantsCaptor.getAllValues().stream().map(Participant::getStudyId)).allMatch(Predicate.isEqual(1L));
        assertThat(participantsCaptor.getAllValues().subList(0,3).stream().map(Participant::getStudyGroupId)).allMatch(Predicate.isEqual(0));
        assertThat(participantsCaptor.getAllValues().subList(3,6).stream().map(Participant::getStudyGroupId)).allMatch(Predicate.isEqual(2));
        assertThat(participantsCaptor.getAllValues().subList(6,8).stream().map(Participant::getStudyGroupId)).allMatch(Predicate.isEqual(4));
        assertThat(participantsCaptor.getAllValues().get(0).getObservationGroupIds()).isEmpty();
        assertThat(participantsCaptor.getAllValues().get(1).getObservationGroupIds()).containsExactlyInAnyOrder(1);
        assertThat(participantsCaptor.getAllValues().get(2).getObservationGroupIds()).containsExactlyInAnyOrder(1, 2);
        assertThat(participantsCaptor.getAllValues().get(3).getObservationGroupIds()).isEmpty();
        assertThat(participantsCaptor.getAllValues().get(4).getObservationGroupIds()).containsExactlyInAnyOrder(2);
        assertThat(participantsCaptor.getAllValues().get(5).getObservationGroupIds()).containsExactlyInAnyOrder(1, 2);
        assertThat(participantsCaptor.getAllValues().get(6).getObservationGroupIds()).containsExactlyInAnyOrder(1);
        assertThat(participantsCaptor.getAllValues().get(7).getObservationGroupIds()).containsExactlyInAnyOrder(2);
    }


    @Test
    @DisplayName("Study export should include Milestones and per-participant ParticipantMilestones")
    void testExportStudy() {
        Long studyId = 1L;

        when(studyService.getStudy(eq(studyId), any()))
                .thenReturn(Optional.of(new Study().setStudyId(studyId)));

        Milestone milestone1 = new Milestone().setMilestoneId(1).setName("Milestone 1").setOrderIndex(0);
        Milestone milestone2 = new Milestone().setMilestoneId(2).setName("Milestone 2").setOrderIndex(1);
        when(milestoneService.listMilestones(studyId)).thenReturn(List.of(milestone1, milestone2));

        Participant participant = new Participant().setStudyId(studyId).setParticipantId(5).setStudyGroupId(2);
        when(participantService.listParticipants(studyId)).thenReturn(List.of(participant));

        Instant milestoneDateTime = Instant.parse("2026-01-15T10:00:00Z");
        when(participantMilestoneService.listParticipantMilestones(studyId, 5)).thenReturn(List.of(
                new ParticipantMilestone().setStudyId(studyId).setParticipantId(5).setMilestoneId(2).setDateTime(milestoneDateTime)));

        StudyImportExport export = importExportService.exportStudy(studyId, currentUser);

        assertThat(export.getMilestones()).containsExactly(milestone1, milestone2);
        assertThat(export.getParticipants()).hasSize(1);
        assertThat(export.getParticipants().get(0).milestones()).containsExactly(
                new ParticipantMilestoneInfo(2, milestoneDateTime));
    }

}
