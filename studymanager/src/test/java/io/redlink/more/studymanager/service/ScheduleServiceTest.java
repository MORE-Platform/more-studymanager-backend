package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.model.Event;
import io.redlink.more.studymanager.model.Timeframe;
import io.redlink.more.studymanager.repository.StudyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class ScheduleServiceTest {

    @MockBean
    StudyRepository studyRepository;

    @Autowired
    @InjectMocks
    ScheduleService scheduleService;


    @Test
    void testAssertPasses() {
        when(studyRepository.getStudyTimeframe(anyLong())).thenReturn(
                new Timeframe(
                        LocalDate.of(2023, 6, 2),
                        LocalDate.of(2023,6,3))
        );
        Event schedule = new Event()
                .setDateStart(Instant.parse("2023-06-02T00:00:00.00Z"))
                .setDateEnd(Instant.parse("2023-06-03T00:00:00.00Z"));
        assertThat(scheduleService.assertScheduleWithinStudyTime(1L, schedule)).isEqualTo(schedule);
    }

    @Test
    void testAssertFails() {
        System.out.println(scheduleService.zoneId);
        when(studyRepository.getStudyTimeframe(anyLong())).thenReturn(
                new Timeframe(
                        LocalDate.of(2023, 6, 2),
                        LocalDate.of(2023,6,3))
        );
        Event scheduleBefore = new Event()
                .setDateStart(Instant.parse("2023-06-01T00:00:00.00Z"))
                .setDateEnd(Instant.parse("2023-06-02T00:00:00.00Z"));
        Event scheduleAfter = new Event()
                .setDateStart(Instant.parse("2023-06-02T00:00:00.00Z"))
                .setDateEnd(Instant.parse("2023-06-04T00:00:00.00Z"));
        assertThrows(BadRequestException.class, () -> scheduleService.assertScheduleWithinStudyTime(1L, scheduleBefore));
        assertThrows(BadRequestException.class, () -> scheduleService.assertScheduleWithinStudyTime(1L, scheduleAfter));
    }
}
