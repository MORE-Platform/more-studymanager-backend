/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
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
