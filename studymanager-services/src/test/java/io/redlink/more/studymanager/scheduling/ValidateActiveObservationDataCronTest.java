package io.redlink.more.studymanager.scheduling;

import io.redlink.more.studymanager.core.datavalidity.ObservationDataState;
import io.redlink.more.studymanager.model.ObservationResyncRequest;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.repository.ObservationResyncRequestRepository;
import io.redlink.more.studymanager.sdk.MoreSDK;
import io.redlink.more.studymanager.service.ObservationService;
import io.redlink.more.studymanager.service.OccurredObservationService;
import io.redlink.more.studymanager.service.ParticipantService;
import io.redlink.more.studymanager.service.StudyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidateActiveObservationDataCronTest {

    private static final long STUDY_ID = 1L;
    private static final int PARTICIPANT_ID = 2;
    private static final int OBSERVATION_ID = 3;

    @Mock
    StudyService studyService;

    @Mock
    ParticipantService participantService;

    @Mock
    OccurredObservationService occurredObservationService;

    @Mock
    ObservationService observationService;

    @Mock
    MoreSDK sdk;

    @Mock
    ObservationResyncRequestRepository resyncRequestRepository;

    @InjectMocks
    ValidateActiveObservationDataCron cron;

    @Test
    void validatesTheObservationOfASyncedRequestAndDeletesIt() {
        when(resyncRequestRepository.listSynced()).thenReturn(List.of(request()));
        when(studyService.getStudy(STUDY_ID, null)).thenReturn(Optional.of(new Study().setStudyId(STUDY_ID)));
        when(occurredObservationService.streamOccurredObservations(
                STUDY_ID, PARTICIPANT_ID, OBSERVATION_ID, true, EnumSet.allOf(ObservationDataState.class)))
                .thenReturn(Stream.empty());

        cron.validateSyncedResyncRequests();

        verify(occurredObservationService).streamOccurredObservations(
                STUDY_ID, PARTICIPANT_ID, OBSERVATION_ID, true, EnumSet.allOf(ObservationDataState.class));
        verify(resyncRequestRepository).deleteSynced(STUDY_ID, PARTICIPANT_ID, OBSERVATION_ID);
    }

    @Test
    void deletesTheSyncedRequestEvenIfTheValidationFails() {
        when(resyncRequestRepository.listSynced()).thenReturn(List.of(request()));
        when(studyService.getStudy(STUDY_ID, null)).thenThrow(new IllegalStateException("db down"));

        cron.validateSyncedResyncRequests();

        verify(resyncRequestRepository).deleteSynced(STUDY_ID, PARTICIPANT_ID, OBSERVATION_ID);
    }

    private static ObservationResyncRequest request() {
        return new ObservationResyncRequest(STUDY_ID, PARTICIPANT_ID, OBSERVATION_ID, Instant.parse("2026-10-08T09:00:00Z"));
    }
}
