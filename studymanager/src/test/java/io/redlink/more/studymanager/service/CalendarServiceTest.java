package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.StudyDurationInfo;
import io.redlink.more.studymanager.model.scheduler.Duration;
import io.redlink.more.studymanager.model.scheduler.Event;
import io.redlink.more.studymanager.model.scheduler.RecurrenceRule;
import io.redlink.more.studymanager.model.scheduler.RelativeDate;
import io.redlink.more.studymanager.model.scheduler.RelativeEvent;
import io.redlink.more.studymanager.model.scheduler.RelativeRecurrenceRule;
import io.redlink.more.studymanager.model.timeline.StudyTimeline;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @Test
    void testStudyNotFound() {
        when(studyService.getStudy(any(), any())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> calendarService.getTimeline(1L, 1, 1, OffsetDateTime.now(), LocalDate.now(), LocalDate.now()));
    }

    @Test
    void testParticipantNotFound() {
        when(participantService.getParticipant(any(), any()))
                .thenReturn(null);
        when(studyService.getStudy(any(), any()))
                .thenReturn(Optional.of(
                        new Study()
                                .setPlannedStartDate(LocalDate.now())
                                .setPlannedEndDate(LocalDate.now().plusDays(3))
                ));
        assertThrows(NotFoundException.class, () -> calendarService.getTimeline(1L, 1,1, OffsetDateTime.now(), LocalDate.now(), LocalDate.now()));
    }

    @Test
    void testGetTimeline() {
        Study study = new Study()
                .setStudyId(1L)
                .setPlannedStartDate(LocalDate.of(2024, 5, 9))
                .setStartDate(LocalDate.of(2024, 5, 10))
                .setPlannedEndDate(LocalDate.of(2024, 5, 14))
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
                .setObservationId(3)
                .setTitle("title3")
                .setPurpose("purpose3")
                .setType("accelerometer3")
                .setSchedule(new RelativeEvent()
                        .setDtstart(new RelativeDate()
                                .setTime("20:00")
                                .setOffset(new Duration().setValue(1).setUnit(Duration.Unit.HOUR)))
                        .setDtend(new RelativeDate()
                                .setTime("21:00")
                                .setOffset(new Duration().setValue(1).setUnit(Duration.Unit.HOUR))))
                .setHidden(false);

        Observation observationRelativeRecurrent = new Observation()
                .setObservationId(4)
                .setTitle("title4")
                .setPurpose("purpose4")
                .setType("accelerometer4")
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
        when(studyService.getStudyDuration(any(), any()))
                .thenReturn(Optional.of(new Duration().setValue(5).setUnit(Duration.Unit.DAY)));
        when(observationService.listObservationsForGroup(any(), eq(participant.getStudyGroupId()))).thenReturn(
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
                LocalDate.of(2024,5,17)
        );

        assertEquals(7, timeline.observationTimelineEvents().size());

    }


    @Test
    void testCreateTimeline() {
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
                .setObservationId(3)
                .setTitle("title3")
                .setPurpose("purpose3")
                .setType("accelerometer3")
                .setSchedule(new RelativeEvent()
                        .setDtstart(new RelativeDate()
                                .setTime("20:00")
                                .setOffset(new Duration().setValue(1).setUnit(Duration.Unit.HOUR)))
                        .setDtend(new RelativeDate()
                                .setTime("21:00")
                                .setOffset(new Duration().setValue(1).setUnit(Duration.Unit.HOUR))))
                .setHidden(false);

        Observation observationRelativeRecurrent = new Observation()
                .setObservationId(4)
                .setTitle("title4")
                .setPurpose("purpose4")
                .setType("accelerometer4")
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

        StudyTimeline timeline = calendarService.createTimeline(
                1L,
                1,
                2,
                OffsetDateTime.of(
                        LocalDate.of(2024, 5, 11),
                        LocalTime.of(10,10,10),
                        OffsetDateTime.now().getOffset()),
                LocalDate.of(2024, 5, 9),
                LocalDate.of(2024,5,17)
            );

        assertEquals(7, timeline.observationTimelineEvents().size());

    }
}
