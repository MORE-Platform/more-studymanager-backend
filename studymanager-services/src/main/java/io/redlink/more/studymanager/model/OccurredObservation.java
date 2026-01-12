package io.redlink.more.studymanager.model;

import io.redlink.more.studymanager.core.datavalidity.ObservationDataState;
import io.redlink.more.studymanager.core.properties.OccurredObservationProperties;

import java.time.Instant;

/**
 * An observation of a participant in the context of a study.
 * Observations are only templates that define what kind ob observation
 * a participant needs perform as part of its participation in a study.
 * The ParticipantObservation is such an instance, where a participant
 * is expected to perform an observation.
 */
public record OccurredObservation(
        Long studyId,
        Integer observationId,
        Integer participantId,
        Instant start,
        Instant end,
        Boolean dataValid,
        ObservationDataState dataState,
        OccurredObservationProperties properties,
        Instant created,
        Instant modified
) {
    public OccurredObservation(
            Long studyId,
            Integer observationId,
            Integer participantId,
            Instant start,
            Instant end,
            Boolean dataValid,
            ObservationDataState dataState,
            OccurredObservationProperties properties
    ) {
        this(studyId, observationId, participantId, start, end, dataValid, dataState, properties, null, null);
    }
    public OccurredObservation(
            Long studyId,
            Integer observationId,
            Integer participantId,
            Instant start,
            Instant end
    ) {
        this(studyId, observationId, participantId, start, end, true, ObservationDataState.MISSING, new OccurredObservationProperties());
    }

}
