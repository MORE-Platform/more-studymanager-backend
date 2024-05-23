package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.StudyDurationInfo;
import io.redlink.more.studymanager.model.scheduler.Duration;
import io.redlink.more.studymanager.model.timeline.ObservationTimelineEvent;
import io.redlink.more.studymanager.model.timeline.StudyTimeline;
import io.redlink.more.studymanager.utils.SchedulerUtils;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Range;
import org.springframework.stereotype.Service;

import static io.redlink.more.studymanager.utils.SchedulerUtils.shiftStartIfObservationAlreadyEnded;


@Service
public class CalendarService {

    private final StudyService studyService;
    private final ObservationService observationService;
    private final InterventionService interventionService;
    private final ParticipantService participantService;
    private final StudyGroupService studyGroupService;

    public CalendarService(StudyService studyService, ObservationService observationService, InterventionService interventionService,
                           ParticipantService participantService, StudyGroupService studyGroupService) {
        this.studyService = studyService;
        this.observationService = observationService;
        this.interventionService = interventionService;
        this.participantService = participantService;
        this.studyGroupService = studyGroupService;
    }

    public StudyTimeline getTimeline(Long studyId, Integer participantId, Integer studyGroupId, OffsetDateTime referenceDate, LocalDate from, LocalDate to) {
        final Study study = studyService.getStudy(studyId, null)
                .orElseThrow(() -> NotFoundException.Study(studyId));
        final Range<LocalDate> studyRange = Range.between(
                Objects.requireNonNullElse(study.getStartDate(), study.getPlannedStartDate()),
                Objects.requireNonNullElse(study.getEndDate(), study.getPlannedEndDate()),
                LocalDate::compareTo
        );
        final Participant participant;
        if (participantId != null) {
            participant = Optional.ofNullable(participantService.getParticipant(studyId, participantId))
                    .orElseThrow(() -> NotFoundException.Participant(studyId, participantId));
        } else {
            participant = null;
        }

        /* Priority of Parameters:
         * participantStart:
         * (1) referenceDate (if provided by user)
         * (2) participant.start (if participant is provided & has started)
         * (3) study.start (if study is started)
         * (4) study.plannedStart
         */
        final Instant participantStart;
        if (referenceDate != null) {
            participantStart = referenceDate.toInstant();
        } else if (participant != null && participant.getStart() != null) {
            participantStart = participant.getStart();
        } else {
            participantStart = studyRange.getMinimum()
                    .atTime(LocalTime.of(9, 0))
                    .atZone(ZoneId.systemDefault())
                    .toInstant();
        }

        /*
         * effectiveGroup:
         * (1) participant.group (if participant is provided)
         * (2) studyGroupId (if provided by user and participant is NOT provided)
         * (3) <unset> otherwise
         */
        final Integer effectiveGroup;
        if (participant != null) {
            effectiveGroup = participant.getStudyGroupId();
        } else {
            effectiveGroup = studyGroupId;
        }

        final List<Observation> observations = observationService.listObservationsForGroup(studyId, effectiveGroup);

        // Shift the effective study-start if the participant would miss a relative observation
        final LocalDate firstDayInStudy = SchedulerUtils.alignStartDateToSignupInstant(participantStart, observations);

        // now how long does the study run?
        final Duration studyDuration = Optional.ofNullable(effectiveGroup)
                .flatMap(eg -> studyService.getStudyDuration(studyId, eg))
                .or(() -> studyService.getStudyDuration(studyId))
                .orElseThrow(() -> NotFoundException.Study(studyId));

        final LocalDate lastDayInStudy = studyDuration
                .addTo(firstDayInStudy);
        // Note: the "lastDayInStudy" *could* be after the "(planned) study end".

        final Range<Instant> effectiveRange = Range.between(
                firstDayInStudy.atTime(LocalTime.MIN).atZone(ZoneId.systemDefault()).toInstant(),
                lastDayInStudy.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant()
        );
        final Range<Instant> filterWindow = Range.between(
                from.atTime(LocalTime.MIN).atZone(ZoneId.systemDefault()).toInstant(),
                to.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant()
        );

        return new StudyTimeline(
                participantStart,
                Range.between(firstDayInStudy, lastDayInStudy, LocalDate::compareTo),
                observations.stream()
                        .flatMap(o -> SchedulerUtils
                                .parseToObservationSchedules(
                                        o.getSchedule(), effectiveRange.getMinimum(), effectiveRange.getMaximum()
                                )
                                .stream()
                                // Disabled client-side filter for now...
                                // .filter(filterWindow::isOverlappedBy)
                                .map(e -> ObservationTimelineEvent.fromObservation(o, e.getMinimum(), e.getMaximum()))
                        )
                        .toList(),
                List.of()
        );
    }

    public StudyTimeline createTimeline(Long studyId, Integer participantId, Integer studyGroupId, OffsetDateTime referenceDate, LocalDate from, LocalDate to) {
        return studyService.getStudy(studyId, null).map( study -> {
            final Integer actualStudyGroupId;
            Instant actualReferenceDate = null;

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

            return new StudyTimeline(
                    actualReferenceDate,
                    Range.between(
                            LocalDate.ofInstant(relStart, ZoneId.systemDefault()),
                            LocalDate.ofInstant(info.getDurationFor(actualStudyGroupId).addTo(relStart), ZoneId.systemDefault()),
                            LocalDate::compareTo
                    ),
                    observations.stream()
                            .map(observation ->
                                    SchedulerUtils.parseToObservationSchedules(observation.getSchedule(), relStart, info.getDurationFor(actualStudyGroupId).addTo(relStart))
                                            .stream()
                                            .filter((event) ->
                                                    event.getMinimum().isBefore(to.atStartOfDay(ZoneId.systemDefault()).toInstant()) &&
                                                    event.getMaximum().isAfter(from.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                                            .map(schedule -> ObservationTimelineEvent.fromObservation(observation, schedule.getMinimum(), schedule.getMaximum()))
                                            .toList()
                            )
                            .flatMap(List::stream)
                            .collect(Collectors.toList()),
                    List.of()
            );
        }).orElse(null);
    }


}
