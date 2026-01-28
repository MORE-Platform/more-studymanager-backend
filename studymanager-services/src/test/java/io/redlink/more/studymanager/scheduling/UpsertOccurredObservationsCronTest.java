package io.redlink.more.studymanager.scheduling;

import io.redlink.more.studymanager.model.OccurredObservation;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.timeline.ObservationTimelineEvent;
import io.redlink.more.studymanager.model.timeline.StudyTimeline;
import io.redlink.more.studymanager.service.CalendarService;
import io.redlink.more.studymanager.service.OccurredObservationService;
import io.redlink.more.studymanager.service.ParticipantService;
import io.redlink.more.studymanager.service.StudyService;
import org.apache.commons.lang3.Range;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UpsertOccurredObservationsCronTest {

    @Mock
    StudyService studyService;

    @Mock
    ParticipantService participantService;

    @Mock
    OccurredObservationService occurredObservationService;

    @Mock
    CalendarService calendarService;

    @InjectMocks
    UpsertOccurredObservationsCron upsertOccurredObservationsCron;


    Instant lastOoTimestamp;
    Instant testNowTimestamp;

    @BeforeEach
    public void beforeEach() {
        testNowTimestamp = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        lastOoTimestamp = testNowTimestamp.minus(5, ChronoUnit.MINUTES);
    }

    @Test
    @DisplayName("Test upsert of occurred observation")
    public void testUpsert() {

        Study study1 = new Study()
                .setStudyId(1L)
                .setTitle("Study 1")
                .setStudyState(Study.Status.ACTIVE);
        Participant study1Par1 = new Participant()
                .setParticipantId(1)
                .setAlias("Study 1 Participant 1")
                .setStatus(Participant.Status.ACTIVE);

        Participant study1Par2 = new Participant()
                .setParticipantId(2)
                .setAlias("Study 1 Participant 2")
                .setStatus(Participant.Status.ACTIVE);
        Participant study1Par3 = new Participant()
                .setParticipantId(3)
                .setAlias("Study 1 Participant 3")
                .setStatus(Participant.Status.ACTIVE);

        Study study2 = new Study()
                .setStudyId(2L)
                .setTitle("Study 2")
                .setStudyState(Study.Status.PREVIEW);
        Participant study2Par1 = new Participant()
                .setParticipantId(1)
                .setAlias("Study 2 Participant 1")
                .setStatus(Participant.Status.ACTIVE);
        Participant study2Par2 = new Participant()
                .setParticipantId(2)
                .setAlias("Study 2 Participant 2")
                .setStatus(Participant.Status.ACTIVE);
        //Mock the iteration over all active studies
        when(studyService.getStudiesByStates(anyIterable()))
                .thenReturn(Stream.of(study1, study2));

        //Mock getting the participants for all active studies
        when(participantService.listParticipants(study1.getStudyId()))
                .thenReturn(List.of(study1Par1, study1Par2, study1Par3));
        when(participantService.listParticipants(study2.getStudyId()))
                .thenReturn(List.of(study2Par1, study2Par2));

        when(calendarService.getTimeline(any(Study.class), any(Participant.class), any(), any(), any(), any(), any()))
                .thenAnswer(invocationOnMock -> {
                    Long studyId = invocationOnMock.getArgument(0, Study.class).getStudyId();
                    Integer participantId = invocationOnMock.getArgument(1, Participant.class).getParticipantId();
                    //Mock Timeline for Study 1 Participant 1
                    // ... we expect a single occurred Observation for observation 2
                    if (Objects.equals(studyId, study1.getStudyId()) && Objects.equals(participantId, study1Par1.getParticipantId())) {
                        return createStudytimeline(List.of(
                                createObservationTimelineEvent(
                                        1,
                                        //this event was activated at the end of the previous run - should be ignored
                                        lastOoTimestamp,
                                        lastOoTimestamp.plus(30, ChronoUnit.MINUTES)
                                ),
                                createObservationTimelineEvent(
                                        2,
                                        //this event was activated after the previous run!
                                        lastOoTimestamp.plus(1, ChronoUnit.MINUTES),
                                        lastOoTimestamp.plus(31, ChronoUnit.MINUTES)
                                ),
                                createObservationTimelineEvent(
                                        3,
                                        //this event is in the future - should be ignored
                                        testNowTimestamp.plus(2, ChronoUnit.MINUTES),
                                        lastOoTimestamp.plus(31, ChronoUnit.MINUTES)
                                )));
                    } else if(Objects.equals(studyId, study1.getStudyId()) && Objects.equals(participantId, study1Par2.getParticipantId())) {
                        //Mock Timeline for Study 1 Participant 2
                        // ... we expect no occurred Observation
                        return createStudytimeline(List.of(
                                createObservationTimelineEvent(
                                        1,
                                        //this event was activated at the end of the previous run - should be ignored
                                        lastOoTimestamp,
                                        testNowTimestamp.plus(14, ChronoUnit.MINUTES)
                                ),
                                createObservationTimelineEvent(
                                        3,
                                        //this event was activated before the previous run!
                                        lastOoTimestamp.minus(1, ChronoUnit.MINUTES),
                                        lastOoTimestamp.plus(29, ChronoUnit.MINUTES)
                                )));
                    } else if(Objects.equals(studyId, study1.getStudyId()) && Objects.equals(participantId, study1Par3.getParticipantId())) {
                        //Mock Timeline for Study 1 Participant 3
                        // ... we expect two occurred Observation (observation 1 and 3)
                        return createStudytimeline(List.of(
                                createObservationTimelineEvent(
                                        1,
                                        //this event was activated after the end of the previous run - should be included
                                        testNowTimestamp.minus(1, ChronoUnit.MINUTES),
                                        testNowTimestamp.plus(14, ChronoUnit.MINUTES)
                                ),
                                createObservationTimelineEvent(
                                        3,
                                        //this event was activated before the previous run!
                                        lastOoTimestamp.plus(1, ChronoUnit.MINUTES),
                                        lastOoTimestamp.plus(31, ChronoUnit.MINUTES)
                                )));
                    } else if(Objects.equals(studyId, study2.getStudyId()) && Objects.equals(participantId, study2Par1.getParticipantId())) {
                        //Mock Timeline for Study 2 Participant 1
                        // ... we expect one occurred Observation (observation 1)
                        return createStudytimeline(List.of(
                                createObservationTimelineEvent(
                                        1,
                                        //this event was activated after the end of the previous run - should be ignored
                                        testNowTimestamp.minus(1, ChronoUnit.MINUTES),
                                        testNowTimestamp.plus(14, ChronoUnit.MINUTES)
                                ),
                                createObservationTimelineEvent(
                                        2,
                                        //this event is one day in the past
                                        lastOoTimestamp.plus(1, ChronoUnit.MINUTES).minus(1, ChronoUnit.DAYS),
                                        lastOoTimestamp.plus(31, ChronoUnit.MINUTES).minus(1, ChronoUnit.DAYS)
                                )));
                    } else if(Objects.equals(studyId, study2.getStudyId()) && Objects.equals(participantId, study2Par2.getParticipantId())) {
                        //Mock Timeline for Study 2 Participant 2
                        // ... we expect one occurred Observation (observation 2)
                        return createStudytimeline(List.of(
                                createObservationTimelineEvent(
                                        1,
                                        //this event is one day in the future
                                        lastOoTimestamp.plus(1, ChronoUnit.MINUTES).plus(1, ChronoUnit.DAYS),
                                        lastOoTimestamp.plus(31, ChronoUnit.MINUTES).plus(1, ChronoUnit.DAYS)
                                ),
                                createObservationTimelineEvent(
                                        2,
                                        //this event was activated after the end of the previous run - should be ignored
                                        testNowTimestamp.minus(1, ChronoUnit.MINUTES),
                                        testNowTimestamp.plus(14, ChronoUnit.MINUTES)
                                )));
                    } else {
                        return createStudytimeline(List.of()); //unexpected ... empty timeline
                    }
                });


        //finally moch the two methods used in the occurredObservationService
        when(occurredObservationService.getLatestStartTime(anyLong()))
                .thenReturn(lastOoTimestamp);

        when(occurredObservationService.upsert(anyLong(), anyInt(), anyInt(), any(), any()))
                .thenAnswer(invocationOnMock ->
                        new OccurredObservation(
                                invocationOnMock.getArgument(0, Long.class),
                                invocationOnMock.getArgument(1, Integer.class),
                                invocationOnMock.getArgument(2, Integer.class),
                                invocationOnMock.getArgument(3, Instant.class),
                                invocationOnMock.getArgument(4, Instant.class)
                        ));

        upsertOccurredObservationsCron.upsertOccurredObservations();

        //Assertions

        //verify the the #getLatestStartTime(..) method is called for each study once
        ArgumentCaptor<Long> studyIdCaptor = ArgumentCaptor.forClass(Long.class);
        verify(occurredObservationService, times(2))
                .getLatestStartTime(studyIdCaptor.capture());
        assertThat(studyIdCaptor.getAllValues()).contains(1L, 2L);

        ArgumentCaptor<Long> ooStudyIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Integer> ooObservationIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> ooParticipantIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Instant> ooStartCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> ooEndCaptor = ArgumentCaptor.forClass(Instant.class);
        //All in all we expect 5 OccurredObservations to be created
        verify(occurredObservationService, times(5)).upsert(
                ooStudyIdCaptor.capture(),
                ooObservationIdCaptor.capture(),
                ooParticipantIdCaptor.capture(),
                ooStartCaptor.capture(),
                ooEndCaptor.capture());
        // Assert the expected values for the 5 expected OccurredObservations
        //  Study 1, participant 1, observation 2
        //  Study 1, participant 3, observation 1
        //  Study 1, participant 3, observation 3
        //  Study 2, participant 1, observation 1
        //  Study 2, participant 2, observation 2
        assertThat(ooStudyIdCaptor.getAllValues()).containsExactly(1L, 1L, 1L,  2L, 2L);
        assertThat(ooObservationIdCaptor.getAllValues()).containsExactly(2, 1, 3, 1,  2);
        assertThat(ooParticipantIdCaptor.getAllValues()).containsExactly(1, 3, 3, 1,  2);
        assertThat(ooStartCaptor.getAllValues()).containsExactly(
                lastOoTimestamp.plus(1, ChronoUnit.MINUTES),
                testNowTimestamp.minus(1, ChronoUnit.MINUTES),
                lastOoTimestamp.plus(1, ChronoUnit.MINUTES),
                testNowTimestamp.minus(1, ChronoUnit.MINUTES),
                testNowTimestamp.minus(1, ChronoUnit.MINUTES)
        );
        assertThat(ooEndCaptor.getAllValues()).containsExactly(
                lastOoTimestamp.plus(31, ChronoUnit.MINUTES),
                testNowTimestamp.plus(14, ChronoUnit.MINUTES),
                lastOoTimestamp.plus(31, ChronoUnit.MINUTES),
                testNowTimestamp.plus(14, ChronoUnit.MINUTES),
                testNowTimestamp.plus(14, ChronoUnit.MINUTES)
        );
    }

    private static StudyTimeline createStudytimeline(List<ObservationTimelineEvent> events) {
        return new StudyTimeline(
                Instant.now(), //bot used
                Range.of(LocalDate.now().minusDays(14), LocalDate.now()), //not used
                events,
                List.of()
        );
    }

    private static ObservationTimelineEvent createObservationTimelineEvent(int observationId, Instant start, Instant end) {
        return new ObservationTimelineEvent(
                observationId,
                1, //not used
                "TimelineEvent for Observation " + observationId,
                "test purpose", //purpose is no used
                "test", //type is not used
                //this event is in the future - should be ignored
                start,
                end,
                false, //not used
                "scheduleType" //not used
        );
    }

}
