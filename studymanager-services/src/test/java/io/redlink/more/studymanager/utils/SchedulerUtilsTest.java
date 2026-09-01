package io.redlink.more.studymanager.utils;

import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.scheduler.Duration;
import io.redlink.more.studymanager.model.scheduler.RelativeDate;
import io.redlink.more.studymanager.model.scheduler.RelativeEvent;
import org.apache.commons.lang3.Range;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SchedulerUtilsTest {
    @Test
    void alignStartDateToSignupInstant() {
        final Observation observation = new Observation()
                .setObservationId(1)
                .setTitle("Early Test Observation")
                .setSchedule(new RelativeEvent()
                        .setDtstart(new RelativeDate()
                                .setOffset(new Duration().setValue(1).setUnit(Duration.Unit.DAY))
                                .setTime(LocalTime.parse("08:00"))
                        )
                        .setDtend(new RelativeDate()
                                .setOffset(new Duration().setValue(1).setUnit(Duration.Unit.DAY))
                                .setTime(LocalTime.parse("09:00"))
                        )
                );

        final LocalDate today = LocalDate.now(ZoneId.systemDefault());
        final Instant beforeObservation = today
                .atTime(LocalTime.parse("07:30"))
                .atZone(ZoneId.systemDefault())
                .toInstant();
        assertThat(SchedulerUtils.alignStartDateToSignupInstant(beforeObservation, List.of(observation)))
                .as("Signup is at %s (before the observation ends), so we start immediately", beforeObservation)
                .isEqualTo(today);

        final Instant duringObservation = today
                .atTime(LocalTime.parse("08:30"))
                .atZone(ZoneId.systemDefault())
                .toInstant();
        assertThat(SchedulerUtils.alignStartDateToSignupInstant(duringObservation, List.of(observation)))
                .as("Signup is at %s (before the observation ends), so we start immediately", duringObservation)
                .isEqualTo(today);

        final Instant afterObservation = today
                .atTime(LocalTime.parse("09:30"))
                .atZone(ZoneId.systemDefault())
                .toInstant();
        assertThat(SchedulerUtils.alignStartDateToSignupInstant(afterObservation, List.of(observation)))
                .as("Signup is at %s (after the observation ends), so we start tomorrow", afterObservation)
                .isEqualTo(today.plusDays(1));


    }

    @Test
    void milestoneRelativeEventOffsets() {
        final LocalDate milestoneDay = LocalDate.of(2024, 6, 15);
        final Instant milestoneDateTime = milestoneDay.atTime(LocalTime.parse("12:00")).atZone(ZoneId.systemDefault()).toInstant();
        final Instant maxEnd = milestoneDateTime.plus(10, ChronoUnit.DAYS);

        final RelativeEvent before = new RelativeEvent()
                .setDtstart(new RelativeDate().setOffset(new Duration().setValue(-1).setUnit(Duration.Unit.DAY)).setTime(LocalTime.parse("08:00")))
                .setDtend(new RelativeDate().setOffset(new Duration().setValue(-1).setUnit(Duration.Unit.DAY)).setTime(LocalTime.parse("09:00")));
        List<Range<Instant>> beforeResult = SchedulerUtils.parseToObservationSchedulesForRelativeEvent(before, milestoneDateTime, maxEnd, true);
        assertThat(beforeResult).hasSize(1);
        assertThat(beforeResult.get(0).getMinimum())
                .as("offset -1 DAY means the day before the milestone")
                .isEqualTo(milestoneDay.minusDays(1).atTime(LocalTime.parse("08:00")).atZone(ZoneId.systemDefault()).toInstant());

        final RelativeEvent exact = new RelativeEvent()
                .setDtstart(new RelativeDate().setOffset(new Duration().setValue(0).setUnit(Duration.Unit.DAY)).setTime(LocalTime.parse("09:00")))
                .setDtend(new RelativeDate().setOffset(new Duration().setValue(0).setUnit(Duration.Unit.DAY)).setTime(LocalTime.parse("10:00")));
        List<Range<Instant>> exactResult = SchedulerUtils.parseToObservationSchedulesForRelativeEvent(exact, milestoneDateTime, maxEnd, true);
        assertThat(exactResult.get(0).getMinimum())
                .as("offset 0 DAY means exactly the milestone's calendar day")
                .isEqualTo(milestoneDay.atTime(LocalTime.parse("09:00")).atZone(ZoneId.systemDefault()).toInstant());

        final RelativeEvent after = new RelativeEvent()
                .setDtstart(new RelativeDate().setOffset(new Duration().setValue(2).setUnit(Duration.Unit.DAY)).setTime(LocalTime.parse("07:00")))
                .setDtend(new RelativeDate().setOffset(new Duration().setValue(2).setUnit(Duration.Unit.DAY)).setTime(LocalTime.parse("08:00")));
        List<Range<Instant>> afterResult = SchedulerUtils.parseToObservationSchedulesForRelativeEvent(after, milestoneDateTime, maxEnd, true);
        assertThat(afterResult.get(0).getMinimum())
                .as("offset +2 DAY means two days after the milestone")
                .isEqualTo(milestoneDay.plusDays(2).atTime(LocalTime.parse("07:00")).atZone(ZoneId.systemDefault()).toInstant());
    }

    @Test
    void participantStartAnchorStillAppliesOneBasedDayCorrection() {
        final LocalDate signupDay = LocalDate.of(2024, 6, 15);
        final Instant signup = signupDay.atStartOfDay(ZoneId.systemDefault()).toInstant();
        final Instant maxEnd = signup.plus(10, ChronoUnit.DAYS);

        // offset value 1 (1-based "first day") must resolve to the signup day itself, not signup+1
        final RelativeEvent firstDay = new RelativeEvent()
                .setDtstart(new RelativeDate().setOffset(new Duration().setValue(1).setUnit(Duration.Unit.DAY)).setTime(LocalTime.parse("08:00")))
                .setDtend(new RelativeDate().setOffset(new Duration().setValue(1).setUnit(Duration.Unit.DAY)).setTime(LocalTime.parse("09:00")));
        List<Range<Instant>> result = SchedulerUtils.parseToObservationSchedulesForRelativeEvent(firstDay, signup, maxEnd, false);
        assertThat(result.get(0).getMinimum())
                .isEqualTo(signupDay.atTime(LocalTime.parse("08:00")).atZone(ZoneId.systemDefault()).toInstant());
    }
}