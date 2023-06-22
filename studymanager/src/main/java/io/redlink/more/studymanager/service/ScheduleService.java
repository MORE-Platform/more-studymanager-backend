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

    public ScheduleService(StudyRepository studyRepository) {
        this.studyRepository = studyRepository;
    }

    public Event assertScheduleWithinStudyTime(Long studyId, Event schedule) {
        Timeframe timeframe = studyRepository.getStudyTimeframe(studyId);
        if(LocalDate.ofInstant(schedule.getDateStart(), ZoneId.systemDefault()).isBefore(timeframe.getFrom())
                || LocalDate.ofInstant(schedule.getDateEnd(), ZoneId.systemDefault()).isAfter(timeframe.getTo())) {
            throw new BadRequestException("Schedule should lie within study timeframe");
        }
        return schedule;
    }
}
