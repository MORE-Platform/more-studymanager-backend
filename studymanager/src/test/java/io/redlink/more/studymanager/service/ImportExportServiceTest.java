/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
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
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.*;
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
                                .setObservationGroupId(1),
                        new Observation()
                                .setObservationId(3)
                                .setTitle("observation Title")
                                .setPurpose("observation purpose")
                                .setParticipantInfo("observation info")
                                .setType("gps-mobile-observation")
                                .setStudyGroupId(null)
                                .setProperties(new ObservationProperties())
                                .setSchedule(new Event())
                                .setObservationGroupId(2)))
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
                                .setObservationGroupId(1),
                        new Intervention()
                                .setInterventionId(3)
                                .setTitle("intervention title")
                                .setPurpose("intervention purpose")
                                .setStudyGroupId(3)
                                .setSchedule(new Event())
                                .setObservationGroupId(2)))
                .setTriggers(Map.of(3, new Trigger()
                        .setType("sth")
                        .setProperties(new TriggerProperties())))
                .setActions(Map.of(2, List.of(new Action()
                        .setType("sth")
                        .setProperties(new ActionProperties()))))
                .setParticipants(List.of(
                        new StudyImportExport.ParticipantInfo(0, null),
                        new StudyImportExport.ParticipantInfo(0, Set.of(1)),
                        new StudyImportExport.ParticipantInfo(0, Set.of(1,2)),
                        new StudyImportExport.ParticipantInfo(2, Set.of()),
                        new StudyImportExport.ParticipantInfo(2, Set.of(2)),
                        new StudyImportExport.ParticipantInfo(2, Set.of(1,2)),
                        new StudyImportExport.ParticipantInfo(4, Set.of(1)),
                        new StudyImportExport.ParticipantInfo(4, Set.of(2))
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

        importExportService.importStudy(studyImport, currentUser);

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


        verify(observationService, times(2)).importObservation(idLongCaptor.capture(), observationCaptor.capture());
        verify(interventionService, times(2)).importIntervention(idLongCaptor.capture(), interventionCaptor.capture(), triggerCaptor.capture(), actionCaptor.capture());
        verify(participantService, times(8)).createParticipant(participantsCaptor.capture());
        verify(integrationService, times(2)).addToken(idLongCaptor.capture(), idIntegerCaptor.capture(), aliasCaptor.capture());

        assertThat(observationCaptor.getAllValues().get(0).getObservationId()).isEqualTo(1);
        assertThat(observationCaptor.getAllValues().get(0).getStudyGroupId()).isEqualTo(3);
        assertThat(observationCaptor.getAllValues().get(0).getObservationGroupId()).isEqualTo(1);
        assertThat(observationCaptor.getAllValues().get(1).getObservationId()).isEqualTo(3);
        assertThat(observationCaptor.getAllValues().get(1).getStudyGroupId()).isEqualTo(null);
        assertThat(observationCaptor.getAllValues().get(1).getObservationGroupId()).isEqualTo(2);

        assertThat(interventionCaptor.getAllValues().get(0).getStudyGroupId()).isEqualTo(2);
        assertThat(interventionCaptor.getAllValues().get(0).getInterventionId()).isEqualTo(2);
        assertThat(interventionCaptor.getAllValues().get(0).getObservationGroupId()).isEqualTo(1);
        assertThat(interventionCaptor.getAllValues().get(1).getStudyGroupId()).isEqualTo(3);
        assertThat(interventionCaptor.getAllValues().get(1).getInterventionId()).isEqualTo(3);
        assertThat(interventionCaptor.getAllValues().get(1).getObservationGroupId()).isEqualTo(2);

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


}
