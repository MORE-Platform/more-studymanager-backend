/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Intervention;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.ParticipantObservationSeed;
import io.redlink.more.studymanager.model.ParticipantWithObservationProperties;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.Trigger;
import io.redlink.more.studymanager.model.scheduler.Duration;
import io.redlink.more.studymanager.model.timeline.InterventionTimelineEvent;
import io.redlink.more.studymanager.model.timeline.ObservationTimelineEvent;
import io.redlink.more.studymanager.model.timeline.StudyTimeline;
import io.redlink.more.studymanager.utils.RandomSchedulerUtils;
import io.redlink.more.studymanager.utils.SchedulerUtils;
import org.apache.commons.lang3.Range;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class CalendarService {

    private final StudyService studyService;
    private final ObservationService observationService;
    private final InterventionService interventionService;
    private final ParticipantService participantService;
    private final ParticipantMilestoneService participantMilestoneService;

    public CalendarService(StudyService studyService, ObservationService observationService, InterventionService interventionService,
                           ParticipantService participantService, ParticipantMilestoneService participantMilestoneService) {
        this.studyService = studyService;
        this.observationService = observationService;
        this.interventionService = interventionService;
        this.participantService = participantService;
        this.participantMilestoneService = participantMilestoneService;
    }

    public StudyTimeline getTimeline(Long studyId, Integer participantId, Integer studyGroupId, Collection<Integer> observationGroupIds, Instant referenceDate, LocalDate from, LocalDate to) {
        final Study study = studyService.getStudy(studyId, null)
                .orElseThrow(() -> NotFoundException.Study(studyId));
        final Participant participant;
        if (participantId != null) {
            participant = Optional.ofNullable(participantService.getParticipant(studyId, participantId))
                    .orElseThrow(() -> NotFoundException.Participant(studyId, participantId));
        } else {
            participant = null;
        }
        return getTimeline(study, participant, studyGroupId, observationGroupIds, referenceDate, from, to);
    }

    public StudyTimeline getTimeline(Study study, Participant participant, Integer studyGroupId, Collection<Integer> observationGroupIds, Instant referenceDate, LocalDate from, LocalDate to) {
        final Range<LocalDate> studyRange = Range.of(
                Objects.requireNonNullElse(study.getStartDate(), study.getPlannedStartDate()),
                Objects.requireNonNullElse(study.getEndDate(), study.getPlannedEndDate()),
                LocalDate::compareTo
        );

        /* Priority of Parameters:
         * participantStart:
         * (1) referenceDate (if provided by user)
         * (2) participant.start (if participant is provided & has started)
         * (3) study.start (if study is started)
         * (4) study.plannedStart
         */
        final Instant participantStart;
        if (referenceDate != null) {
            participantStart = referenceDate;
        } else if (participant != null && participant.getStart() != null) {
            participantStart = participant.getStart();
        } else {
            participantStart = studyRange.getMinimum()
                    .atTime(LocalTime.of(9, 0))
                    .atZone(ZoneId.systemDefault())
                    .toInstant();
        }

        /*
         * effectiveStudyGroups:
         * (1) participant.group (if participant is provided)
         * (2) studyGroupId (if provided by user and participant is NOT provided)
         * (3) <unset> otherwise
         */
        final Integer effectiveStudyGroups;
        if (participant != null) {
            effectiveStudyGroups = participant.getStudyGroupId();
        } else {
            effectiveStudyGroups = studyGroupId;
        }
        final Collection<Integer> effectiveObservationGroups;
        if (participant != null) {
            effectiveObservationGroups = participant.getObservationGroupIds();
        } else {
            effectiveObservationGroups = observationGroupIds == null ? Collections.emptyList() : observationGroupIds;
        }

        final List<Observation> observations = observationService.listObservationsForGroup(study.getStudyId(), effectiveStudyGroups, effectiveObservationGroups);
        final List<Intervention> interventions = interventionService.listInterventionsForGroup(study.getStudyId(), effectiveStudyGroups, effectiveObservationGroups);

        // Shift the effective study-start if the participant would miss a relative observation
        final LocalDate firstDayInStudy = SchedulerUtils.alignStartDateToSignupInstant(participantStart, observations);

        // now how long does the study run?
        final Duration studyDuration = Optional.ofNullable(effectiveStudyGroups)
                .flatMap(eg -> studyService.getStudyDuration(study.getStudyId(), eg))
                .or(() -> Optional.ofNullable(study.getDuration()))
                .or(() -> Optional.ofNullable(new Duration()
                        .setValue(
                                (int) ChronoUnit.DAYS.between(
                                        Objects.requireNonNullElse(study.getStartDate(), study.getPlannedStartDate()),
                                        Objects.requireNonNullElse(study.getEndDate(), study.getPlannedEndDate())
                                ) + 1)
                        .setUnit(Duration.Unit.DAY)
                ))
                .orElseThrow(() -> NotFoundException.Study(study.getStudyId()));

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
        //final Range<Instant> filterWindow = Range.of(
        //        from == null ? Instant.MIN : from.atTime(LocalTime.MIN).atZone(ZoneId.systemDefault()).toInstant(),
        //        to == null ? Instant.MAX : to.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant()
        //);

        var properties = observationService.getParticipantObservationProperties(study.getStudyId())
                .stream()
                .filter(p -> participant == null || participant.getParticipantId() == null || p.participantId().equals(participant.getParticipantId()))
                .map(CalendarService::toParticipantObservationSeed).toList();


        return new StudyTimeline(
                participantStart,
                Range.of(firstDayInStudy, lastDayInStudy, LocalDate::compareTo),
                observations.stream()
                        .flatMap(o -> {
                            List<ParticipantObservationSeed> matchingSeeds = properties.stream()
                                    .filter(p -> Objects.equals(o.getObservationId(), p.observationId()))
                                    .toList();

                            if (o.getMilestoneId() == null) {
                                return observationEvents(o, effectiveRange.getMinimum(), false, matchingSeeds, effectiveRange.getMaximum());
                            }

                            if (participant != null) {
                                return participantMilestoneService
                                        .findParticipantMilestone(study.getStudyId(), participant.getParticipantId(), o.getMilestoneId())
                                        // participant hasn't reached this milestone yet: no occurrences
                                        .map(pm -> observationEvents(o, pm.getDateTime(), true, matchingSeeds, effectiveRange.getMaximum()))
                                        .orElseGet(Stream::empty);
                            }

                            // no participant selected: emit occurrences for every participant that has reached this milestone,
                            // each anchored to their own milestone date
                            return participantMilestoneService.listParticipantsForMilestone(study.getStudyId(), o.getMilestoneId())
                                    .stream()
                                    .flatMap(pm -> {
                                        List<ParticipantObservationSeed> seedsForParticipant = matchingSeeds.stream()
                                                .filter(s -> Objects.equals(s.participant(), pm.getParticipantId()))
                                                .toList();
                                        return observationEvents(o, pm.getDateTime(), true, seedsForParticipant, effectiveRange.getMaximum());
                                    });
                        })
                        .toList(),
                interventions.stream()
                        .flatMap(intervention -> {
                            Trigger trigger = interventionService.getTriggerByIds(study.getStudyId(), intervention.getInterventionId());
                            return interventionEvents(study, participant, intervention, trigger, effectiveRange);
                        })
                        .collect(Collectors.toList())

        );
    }

    private Stream<InterventionTimelineEvent> interventionEvents(
            Study study, Participant participant, Intervention intervention, Trigger trigger, Range<Instant> effectiveRange) {
        boolean milestoneApplies = intervention.getMilestoneId() != null
                && trigger != null
                && Objects.equals(trigger.getType(), "relative-time-trigger");

        if (!milestoneApplies) {
            return SchedulerUtils.parseToInterventionSchedules(
                            trigger, effectiveRange.getMinimum(), effectiveRange.getMaximum(), false)
                    .stream()
                    // Disabled client-side filter for now...
                    // .filter(filterWindow::contains)
                    .map(event -> InterventionTimelineEvent.fromInterventionAndTrigger(intervention, trigger, event));
        }

        if (participant != null) {
            return participantMilestoneService
                    .findParticipantMilestone(study.getStudyId(), participant.getParticipantId(), intervention.getMilestoneId())
                    // participant hasn't reached this milestone yet: no occurrences
                    .map(pm -> SchedulerUtils.parseToInterventionSchedules(trigger, pm.getDateTime(), effectiveRange.getMaximum(), true))
                    .map(events -> events.stream()
                            .map(event -> InterventionTimelineEvent.fromInterventionAndTrigger(intervention, trigger, event)))
                    .orElseGet(Stream::empty);
        }

        // no participant selected: emit occurrences for every participant that has reached this milestone,
        // each anchored to their own milestone date
        return participantMilestoneService.listParticipantsForMilestone(study.getStudyId(), intervention.getMilestoneId())
                .stream()
                .flatMap(pm -> SchedulerUtils.parseToInterventionSchedules(trigger, pm.getDateTime(), effectiveRange.getMaximum(), true)
                        .stream()
                        .map(event -> InterventionTimelineEvent.fromInterventionAndTrigger(intervention, trigger, event)));
    }

    private static Stream<ObservationTimelineEvent> observationEvents(
            Observation observation, Instant anchor, boolean isMilestoneAnchor,
            List<ParticipantObservationSeed> seeds, Instant rangeEnd) {
        List<ParticipantObservationSeed> seedsToUse = seeds.isEmpty() ? Collections.singletonList(null) : seeds;
        return seedsToUse.stream()
                .flatMap(seed ->
                        SchedulerUtils
                                .parseToObservationSchedules(seed, observation.getSchedule(), anchor, rangeEnd, isMilestoneAnchor)
                                .stream()
                                // Disabled client-side filter for now...
                                // .filter(filterWindow::isOverlappedBy)
                                .map(e -> ObservationTimelineEvent.fromObservation(observation, e.getMinimum(), e.getMaximum()))
                );
    }

    public static ParticipantObservationSeed toParticipantObservationSeed(ParticipantWithObservationProperties participantWithObservationProperties) {
        return new ParticipantObservationSeed(
                participantWithObservationProperties.studyId(),
                participantWithObservationProperties.participantId(),
                participantWithObservationProperties.observationId(),
                (Long) participantWithObservationProperties.properties().getOrDefault(RandomSchedulerUtils.OBSERVATION_SCHEDULE_SEED_KEY, null));
    }

}
