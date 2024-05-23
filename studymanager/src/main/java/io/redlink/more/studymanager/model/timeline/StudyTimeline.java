package io.redlink.more.studymanager.model.timeline;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.apache.commons.lang3.Range;

public record StudyTimeline(
        Instant signup,
        Range<LocalDate> participationRange,
        List<ObservationTimelineEvent> observationTimelineEvents,
        List<InterventionTimelineEvent> interventionTimelineEvents
) {
}
