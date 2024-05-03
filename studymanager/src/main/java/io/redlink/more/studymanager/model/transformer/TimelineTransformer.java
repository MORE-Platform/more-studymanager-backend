package io.redlink.more.studymanager.model.transformer;


import io.redlink.more.studymanager.api.v1.model.InterventionTimelineEventDTO;
import io.redlink.more.studymanager.api.v1.model.ObservationTimelineEventDTO;
import io.redlink.more.studymanager.api.v1.model.StudyTimelineDTO;
import io.redlink.more.studymanager.model.timeline.InterventionTimelineEvent;
import io.redlink.more.studymanager.model.timeline.ObservationTimelineEvent;
import io.redlink.more.studymanager.model.timeline.StudyTimeline;


public class TimelineTransformer {
    private TimelineTransformer() {}

    public static StudyTimelineDTO toStudyTimelineDTO(StudyTimeline studyTimeline) {
        return new StudyTimelineDTO()
                .observations(studyTimeline.getObservationTimelineEvents().stream().map(
                        TimelineTransformer::toObservationTimelineDTO
                ).toList())
                .interventions(studyTimeline.getInterventionTimelineEvents().stream().map(
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
                .start(Transformers.toOffsetDateTime(observationTimelineEvent.start()))
                .end(Transformers.toOffsetDateTime(observationTimelineEvent.end()))
                .hidden(observationTimelineEvent.hidden())
                .scheduleType(observationTimelineEvent.scheduleType());
    }

    public static InterventionTimelineEventDTO toInterventionTimelineEventDTO(InterventionTimelineEvent interventionTimelineEvent) {
        return new InterventionTimelineEventDTO()
                .interventionId(interventionTimelineEvent.interventionId())
                .studyGroupId(interventionTimelineEvent.studyGroupId())
                .title(interventionTimelineEvent.title())
                .purpose(interventionTimelineEvent.purpose())
                .type(interventionTimelineEvent.type())
                .start(Transformers.toOffsetDateTime(interventionTimelineEvent.start()))
                .end(Transformers.toOffsetDateTime(interventionTimelineEvent.end()))
                .scheduleType(interventionTimelineEvent.scheduleType());
    }
}
