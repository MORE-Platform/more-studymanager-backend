package io.redlink.more.studymanager.model.timeline;

import java.time.Instant;

public record ObservationTimelineEvent(
        Integer observationId,
        Integer studyGroupId,
        String title,
        String purpose,
        String type,
        Instant start,
        Instant end,
        Boolean hidden,
        String scheduleType
) {}
