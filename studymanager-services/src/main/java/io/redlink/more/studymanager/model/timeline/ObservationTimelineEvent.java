/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model.timeline;

import io.redlink.more.studymanager.model.Observation;
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
) {
    public static ObservationTimelineEvent fromObservation(Observation observation, Instant start, Instant end) {
        return new ObservationTimelineEvent(
                observation.getObservationId(),
                observation.getStudyGroupId(),
                observation.getTitle(),
                observation.getPurpose(),
                observation.getType(),
                start,
                end,
                observation.getHidden(),
                observation.getSchedule().getType()
        );
    }
}
