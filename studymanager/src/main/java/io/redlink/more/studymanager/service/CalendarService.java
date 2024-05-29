package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Intervention;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.Trigger;
import io.redlink.more.studymanager.model.scheduler.Duration;
import io.redlink.more.studymanager.model.timeline.InterventionTimelineEvent;
import io.redlink.more.studymanager.model.timeline.ObservationTimelineEvent;
import io.redlink.more.studymanager.model.timeline.StudyTimeline;
import io.redlink.more.studymanager.utils.SchedulerUtils;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Range;
import org.springframework.stereotype.Service;


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

    public StudyTimeline getTimeline(Long studyId, Integer participantId, Integer studyGroupId, OffsetDateTime referenceDate, LocalDate from, LocalDate to) {
        final Study study = studyService.getStudy(studyId, null)
                .orElseThrow(() -> NotFoundException.Study(studyId));
        final Range<LocalDate> studyRange = Range.of(
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
        final List<Intervention> interventions = interventionService.listInterventionsForGroup(studyId, effectiveGroup);

        // Shift the effective study-start if the participant would miss a relative observation
        final LocalDate firstDayInStudy = SchedulerUtils.alignStartDateToSignupInstant(participantStart, observations);

        // now how long does the study run?
        final Duration studyDuration = Optional.ofNullable(effectiveGroup)
                .flatMap(eg -> studyService.getStudyDuration(studyId, eg))
                .or(() -> studyService.getStudyDuration(studyId))
                .orElseThrow(() -> NotFoundException.Study(studyId));

        final LocalDate lastDayInStudy = firstDayInStudy
                .plus(
                        // firstDay / lastDay are *inclusive* bounds, therefor we use the "-1" here
                        Math.max(studyDuration.getValue() - 1, 0),
                        studyDuration.getUnit().toChronoUnit()
                );
        // Note: the "lastDayInStudy" *could* be after the "(planned) study end", but that's OK

        final Range<Instant> effectiveRange = Range.of(
                firstDayInStudy.atTime(LocalTime.MIN).atZone(ZoneId.systemDefault()).toInstant(),
                lastDayInStudy.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant()
        );
        final Range<Instant> filterWindow = Range.of(
                from.atTime(LocalTime.MIN).atZone(ZoneId.systemDefault()).toInstant(),
                to.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant()
        );

        return new StudyTimeline(
                participantStart,
                Range.of(firstDayInStudy, lastDayInStudy, LocalDate::compareTo),
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
                interventions.stream()
                        .map(intervention -> {
                            Trigger trigger = interventionService.getTriggerByIds(studyId, intervention.getInterventionId());
                            return SchedulerUtils.parseToInterventionSchedules(
                                            trigger,
                                            effectiveRange.getMinimum(),
                                            effectiveRange.getMaximum()
                                    )
                                    .stream()
                                    // Disabled client-side filter for now...
                                    // .filter(filterWindow::contains)
                                    .map(event -> InterventionTimelineEvent.fromInterventionAndTrigger(intervention, trigger, event))
                                    .toList();
                        })
                        .flatMap(List::stream)
                        .collect(Collectors.toList())

        );
    }

}
