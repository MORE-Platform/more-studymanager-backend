package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.model.*;
import io.redlink.more.studymanager.model.scheduler.*;
import io.redlink.more.studymanager.model.scheduler.Duration;
import io.redlink.more.studymanager.model.timeline.StudyTimeline;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {
    @Mock
    StudyService studyService;

    @Mock
    ObservationService observationService;

    @Mock
    InterventionService interventionService;

    @Mock
    ParticipantService participantService;

    @InjectMocks
    CalendarService calendarService;

    private final AuthenticatedUser currentUser = new AuthenticatedUser(
            UUID.randomUUID().toString(),
            "Test User", "test@example.com", "Test Inc.",
            EnumSet.allOf(PlatformRole.class)
    );

    @Test
    void testStudyNotFound() {
        when(studyService.getStudy(any(), any())).thenReturn(Optional.empty());
        assertNull(calendarService.getTimeline(1L, 1, 1, OffsetDateTime.now(), LocalDate.now(), LocalDate.now(), currentUser));
    }

    @Test
    void testParticipantNotFound() {
        when(participantService.getParticipant(any(),any())).thenReturn(null);
        when(studyService.getStudy(any(), any())).thenReturn(Optional.of(new Study()));
        assertNull(calendarService.getTimeline(1L, 1,1, OffsetDateTime.now(), LocalDate.now(), LocalDate.now(), currentUser));
    }

    @Test
    void testGetTimeline() {
        Study study = new Study()
                .setStudyId(1L)
                .setPlannedStartDate(LocalDate.of(2024, 5, 9))
                .setStartDate(LocalDate.of(2024, 5, 10))
                .setDuration(new Duration().setUnit(Duration.Unit.DAY).setValue(5));

        Participant participant = new Participant().setStudyGroupId(2);

        Observation observationAbsolute = new Observation()
                .setObservationId(1)
                .setTitle("title")
                .setPurpose("purpose")
                .setType("accelerometer")
                .setSchedule(new Event()
                        .setDateStart(LocalDate.of(2024, 5, 10).atTime(16, 10).atZone(ZoneId.systemDefault()).toInstant())
                        .setDateEnd(LocalDate.of(2024, 5, 10).atTime(18, 10).atZone(ZoneId.systemDefault()).toInstant()))
                .setHidden(true);

        Observation observationAbsoluteRecurrent = new Observation()
                .setObservationId(2)
                .setTitle("title2")
                .setPurpose("purpose2")
                .setType("accelerometer2")
                .setSchedule(new Event()
                        .setDateStart(LocalDate.of(2024, 5, 10).atTime(16, 10).atZone(ZoneId.systemDefault()).toInstant())
                        .setDateEnd(LocalDate.of(2024, 5, 10).atTime(18, 10).atZone(ZoneId.systemDefault()).toInstant())
                        .setRRule(new RecurrenceRule()
                                .setFreq("DAILY")
                                .setCount(2)))
                .setHidden(false);

        Observation observationRelative = new Observation()
                .setObservationId(2)
                .setTitle("title2")
                .setPurpose("purpose2")
                .setType("accelerometer2")
                .setSchedule(new RelativeEvent()
                        .setDtstart(new RelativeDate()
                                .setTime("20:00")
                                .setOffset(new Duration().setValue(1).setUnit(Duration.Unit.HOUR)))
                        .setDtend(new RelativeDate()
                                .setTime("21:00")
                                .setOffset(new Duration().setValue(1).setUnit(Duration.Unit.HOUR))))
                .setHidden(false);

        Observation observationRelativeRecurrent = new Observation()
                .setObservationId(2)
                .setTitle("title2")
                .setPurpose("purpose2")
                .setType("accelerometer2")
                .setSchedule(new RelativeEvent()
                        .setDtstart(new RelativeDate()
                                .setTime("20:00")
                                .setOffset(new Duration().setValue(1).setUnit(Duration.Unit.HOUR)))
                        .setDtend(new RelativeDate()
                                .setTime("21:00")
                                .setOffset(new Duration().setValue(1).setUnit(Duration.Unit.HOUR)))
                        .setRrrule(new RelativeRecurrenceRule()
                                .setFrequency(new Duration().setValue(1).setUnit(Duration.Unit.DAY))
                                .setEndAfter(new Duration().setValue(6).setUnit(Duration.Unit.DAY))))
                .setHidden(false);

        when(studyService.getStudy(any(), any())).thenReturn(Optional.of(study));
        when(participantService.getParticipant(any(), any())).thenReturn(participant);
        when(studyService.getStudyDurationInfo(any())).thenReturn(Optional.of(
                new StudyDurationInfo()
                        .addGroupDuration(Pair.of(2, new Duration().setValue(5).setUnit(Duration.Unit.DAY)))
                        .setDuration(new Duration().setValue(1).setUnit(Duration.Unit.DAY))));
        when(observationService.listObservations(any())).thenReturn(
                List.of(observationAbsolute, observationAbsoluteRecurrent, observationRelative, observationRelativeRecurrent));

        StudyTimeline timeline = calendarService.getTimeline(
                1L,
                1,
                2,
                OffsetDateTime.of(
                        LocalDate.of(2024, 5, 11),
                        LocalTime.of(10,10,10),
                        OffsetDateTime.now().getOffset()),
                LocalDate.of(2024, 5, 9),
                LocalDate.of(2024,5,17),
                currentUser);

        assertEquals(7, timeline.getObservationTimelineEvents().size());

    }
}
