package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.model.Participant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

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

    @Mock
    private ObservationService observationService;

    @Mock
    private InterventionService interventionService;

    @Mock
    private StudyGroupService studyGroupService;


    @Captor
    private ArgumentCaptor<Participant> participantsCaptor;

    @Test
    @DisplayName("CSV should be imported line by line as Participant (header line skipped)")
    void testImportParticipants() throws FileNotFoundException {
        File file = ResourceUtils.getFile("classpath:import/participants-groups-test2.csv");
        ImportExportService service = new ImportExportService(participantService, studyService, studyStateService, observationService, interventionService, studyGroupService);

        service.importParticipants(1L, new FileInputStream(file));

        verify(participantService, times(4)).createParticipant(participantsCaptor.capture());
        assertThat(participantsCaptor.getValue().getAlias()).isEqualTo("more than 2 words");
    }
}
