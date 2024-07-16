/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model.transformer;


import io.redlink.more.studymanager.api.v1.model.InterventionTimelineEventDTO;
import io.redlink.more.studymanager.api.v1.model.ObservationTimelineEventDTO;
import io.redlink.more.studymanager.api.v1.model.StudyTimelineDTO;
import io.redlink.more.studymanager.api.v1.model.StudyTimelineStudyDurationDTO;
import io.redlink.more.studymanager.model.timeline.InterventionTimelineEvent;
import io.redlink.more.studymanager.model.timeline.ObservationTimelineEvent;
import io.redlink.more.studymanager.model.timeline.StudyTimeline;


public final class TimelineTransformer {
    private TimelineTransformer() {}

    public static StudyTimelineDTO toStudyTimelineDTO(StudyTimeline studyTimeline) {
        return new StudyTimelineDTO()
                .participantSignup(studyTimeline.signup())
                .studyDuration(
                        new StudyTimelineStudyDurationDTO()
                                .from(studyTimeline.participationRange().getMinimum())
                                .to(studyTimeline.participationRange().getMaximum())
                )
                .observations(studyTimeline.observationTimelineEvents().stream().map(
                        TimelineTransformer::toObservationTimelineDTO
                ).toList())
                .interventions(studyTimeline.interventionTimelineEvents().stream().map(
                        TimelineTransformer::toInterventionTimelineEventDTO
                ).toList());
    }

    public static ObservationTimelineEventDTO toObservationTimelineDTO(ObservationTimelineEvent observationTimelineEvent) {
        return new ObservationTimelineEventDTO()
                .observationId(observationTimelineEvent.observationId())
                .studyGroupId(observationTimelineEvent.studyGroupId())
                .title(observationTimelineEvent.title())
                .purpose(observationTimelineEvent.purpose())
                .type(observationTimelineEvent.type())
                .start(observationTimelineEvent.start())
                .end(observationTimelineEvent.end())
                .hidden(observationTimelineEvent.hidden())
                .scheduleType(observationTimelineEvent.scheduleType());
    }

    public static InterventionTimelineEventDTO toInterventionTimelineEventDTO(InterventionTimelineEvent interventionTimelineEvent) {
        return new InterventionTimelineEventDTO()
                .interventionId(interventionTimelineEvent.interventionId())
                .studyGroupId(interventionTimelineEvent.studyGroupId())
                .title(interventionTimelineEvent.title())
                .purpose(interventionTimelineEvent.purpose())
                .start(interventionTimelineEvent.start())
                .scheduleType(interventionTimelineEvent.scheduleType());
    }
}
