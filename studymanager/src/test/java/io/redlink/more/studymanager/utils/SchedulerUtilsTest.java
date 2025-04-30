package io.redlink.more.studymanager.utils;

import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.scheduler.Duration;
import io.redlink.more.studymanager.model.scheduler.RelativeDate;
import io.redlink.more.studymanager.model.scheduler.RelativeEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
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
}