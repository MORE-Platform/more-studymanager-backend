package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.StudyDurationInfo;
import io.redlink.more.studymanager.model.User;
import io.redlink.more.studymanager.model.timeline.ObservationTimelineEvent;
import io.redlink.more.studymanager.model.timeline.StudyTimeline;
import io.redlink.more.studymanager.utils.SchedulerUtils;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.redlink.more.studymanager.utils.SchedulerUtils.shiftStartIfObservationAlreadyEnded;


@Service
public class CalendarService {

    private final StudyService studyService;
    private final ObservationService observationService;
    private final InterventionService interventionService;
    private final ParticipantService participantService;
    public CalendarService(StudyService studyService, ObservationService observationService, InterventionService interventionService,
                            ParticipantService participantService) {
        this.studyService = studyService;
        this.observationService = observationService;
        this.interventionService = interventionService;
        this.participantService = participantService;
    }

    public StudyTimeline getTimeline(Long studyId, Integer participantId, Integer studyGroupId, OffsetDateTime referenceDate, LocalDate from, LocalDate to, User currentUser) {
        return studyService.getStudy(studyId, currentUser).map( study -> {
            final Integer actualStudyGroupId;
            Instant actualReferenceDate = null;
            StudyTimeline studyTimeline = new StudyTimeline();

            if(participantId != null) {
                Participant participant = participantService.getParticipant(studyId, participantId);
                if(participant == null)
                    return null;
                actualStudyGroupId = participant.getStudyGroupId();
                if (participant.getStart() != null) {
                    actualReferenceDate = participant.getStart();
                }
            } else {
                actualStudyGroupId = studyGroupId;
            }

            if(actualReferenceDate == null) {
                if (referenceDate != null) {
                    actualReferenceDate = referenceDate.toInstant();
                } else if (study.getStartDate() != null) {
                    actualReferenceDate = study.getStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant();
                } else {
                    actualReferenceDate = study.getPlannedStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant();
                }
            }

            List<Observation> observations = observationService.listObservations(studyId)
                    .stream()
                    .filter(observation -> actualStudyGroupId == null || observation.getStudyGroupId() == null || Objects.equals(observation.getStudyGroupId(), actualStudyGroupId))
                    .toList();
            final Instant relStart = shiftStartIfObservationAlreadyEnded(actualReferenceDate, observations);

            StudyDurationInfo info = studyService.getStudyDurationInfo(studyId)
                    .orElseThrow(() -> new BadRequestException("Cannot create Timeline: missing duration info for study"));

            studyTimeline.addAllObservations(
                    observations.stream()
                            .map(observation ->
                                    SchedulerUtils.parseToObservationSchedules(observation.getSchedule(), relStart, info.getDurationFor(actualStudyGroupId).getEnd(relStart))
                                            .stream()
                                            .filter((event) ->
                                                    event.getKey().isBefore(to.atStartOfDay(ZoneId.systemDefault()).toInstant()) &&
                                                    event.getValue().isAfter(from.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                                            .map(schedule -> ObservationTimelineEvent.fromObservation(observation, schedule.getKey(), schedule.getValue()))
                                            .toList()
                            )
                            .flatMap(List::stream)
                            .collect(Collectors.toList())
            );
            return studyTimeline;
        }).orElse(null);
    }


}
