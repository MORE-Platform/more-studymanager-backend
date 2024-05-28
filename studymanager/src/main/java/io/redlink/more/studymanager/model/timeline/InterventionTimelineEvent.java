package io.redlink.more.studymanager.model.timeline;

import io.redlink.more.studymanager.model.Intervention;
import io.redlink.more.studymanager.model.Trigger;

import java.time.Instant;

public record InterventionTimelineEvent(
        Integer interventionId,
        Integer studyGroupId,
        String title,
        String purpose,
        Instant start,
        String scheduleType
) {
    public static InterventionTimelineEvent fromInterventionAndTrigger(Intervention intervention, Trigger trigger, Instant start) {
        return new InterventionTimelineEvent(
                intervention.getInterventionId(),
                intervention.getStudyGroupId(),
                intervention.getTitle(),
                intervention.getPurpose(),
                start,
                trigger.getType()
        );
    }
}
