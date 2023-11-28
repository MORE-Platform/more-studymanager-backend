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
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImportExportServiceTest {

    @Spy
    private ParticipantService participantService = mock(ParticipantService.class);

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
    private ArgumentCaptor<Action> actionCaptor;

    @Captor
    private ArgumentCaptor<Integer> idCaptor;

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
                                .setTitle("observation Title")
                                .setPurpose("observation purpose")
                                .setParticipantInfo("observation info")
                                .setType("gps-mobile-observation")
                                .setStudyGroupId(3)
                                .setProperties(new ObservationProperties())
                                .setSchedule(new Event()),
                        new Observation()
                                .setTitle("observation Title")
                                .setPurpose("observation purpose")
                                .setParticipantInfo("observation info")
                                .setType("gps-mobile-observation")
                                .setStudyGroupId(null)
                                .setProperties(new ObservationProperties())
                                .setSchedule(new Event())))
                .setStudyGroups(List.of(
                        new StudyGroup()
                                .setStudyGroupId(2)
                                .setTitle("group title")
                                .setPurpose("group purpose"),
                        new StudyGroup()
                                .setStudyGroupId(3)
                                .setTitle("group title2")
                                .setPurpose("group purpose2")))
                .setInterventions(List.of(
                        new Intervention()
                                .setInterventionId(2)
                                .setTitle("intervention title")
                                .setPurpose("intervention purpose")
                                .setStudyGroupId(2)
                                .setSchedule(new Event()),
                        new Intervention()
                                .setInterventionId(3)
                                .setTitle("intervention title")
                                .setPurpose("intervention purpose")
                                .setStudyGroupId(3)
                                .setSchedule(new Event())))
                .setTriggers(Map.of(3, new Trigger()
                        .setType("sth")
                        .setProperties(new TriggerProperties())))
                .setActions(Map.of(2, List.of(new Action()
                        .setType("sth")
                        .setProperties(new ActionProperties()))));

        when(studyService.createStudy(any(), any()))
                .thenAnswer(invocationOnMock ->
                        ((Study) invocationOnMock.getArgument(0)).setStudyId(1L));
        when(studyGroupService.createStudyGroup(any()))
                .thenAnswer(invocationOnMock ->
                        ((StudyGroup) invocationOnMock.getArgument(0)).setStudyGroupId(
                                ((StudyGroup) invocationOnMock.getArgument(0)).getStudyGroupId()-1));
        when(interventionService.addIntervention(interventionCaptor.capture())).thenAnswer(
                invocationOnMock ->
                        ((Intervention) invocationOnMock.getArgument(0)).setInterventionId(
                                ((Intervention) invocationOnMock.getArgument(0)).getInterventionId()-1));

        importExportService.importStudy(studyImport, currentUser);

        verify(observationService, times(2)).addObservation(observationCaptor.capture());
        verify(interventionService, times(1)).updateTrigger(any(), idCaptor.capture(), triggerCaptor.capture());
        verify(interventionService, times(1)).createAction(any(), idCaptor.capture(), actionCaptor.capture());

        assertThat(observationCaptor.getAllValues().get(0).getStudyId()).isEqualTo(1L);
        assertThat(observationCaptor.getAllValues().get(0).getStudyGroupId()).isEqualTo(2);
        assertThat(observationCaptor.getAllValues().get(1).getStudyGroupId()).isEqualTo(null);

        assertThat(interventionCaptor.getAllValues().get(0).getStudyId()).isEqualTo(1L);
        assertThat(interventionCaptor.getAllValues().get(0).getStudyGroupId()).isEqualTo(1);
        assertThat(interventionCaptor.getAllValues().get(1).getStudyGroupId()).isEqualTo(2);

        assertThat(idCaptor.getAllValues().get(0)).isEqualTo(2);
        assertThat(idCaptor.getAllValues().get(1)).isEqualTo(1);
    }


}
