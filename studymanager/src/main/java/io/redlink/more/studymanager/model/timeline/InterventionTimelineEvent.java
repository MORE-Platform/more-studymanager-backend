package io.redlink.more.studymanager.model.timeline;

import java.time.Instant;

public record InterventionTimelineEvent(
        Integer interventionId,
        Integer studyGroupId,
        String title,
        String purpose,
        String type,
        Instant start,
        Instant end,
        String scheduleType
) {}
