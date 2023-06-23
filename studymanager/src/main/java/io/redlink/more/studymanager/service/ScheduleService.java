package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.model.Event;
import io.redlink.more.studymanager.model.Timeframe;
import io.redlink.more.studymanager.repository.StudyRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class ScheduleService {
    private final StudyRepository studyRepository;
    public final ZoneId zoneId;

    public ScheduleService(StudyRepository studyRepository, ZoneId zoneId) {
        this.studyRepository = studyRepository;
        this.zoneId = zoneId;
    }

    public Event assertScheduleWithinStudyTime(Long studyId, Event schedule) {
        Timeframe timeframe = studyRepository.getStudyTimeframe(studyId);
        if(LocalDate.ofInstant(schedule.getDateStart(), zoneId).isBefore(timeframe.from())
                || LocalDate.ofInstant(schedule.getDateEnd(), zoneId).isAfter(timeframe.to())) {
            throw new BadRequestException("Schedule should lie within study timeframe");
        }
        return schedule;
    }
}
