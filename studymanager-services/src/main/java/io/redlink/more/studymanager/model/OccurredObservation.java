package io.redlink.more.studymanager.model;

import io.redlink.more.studymanager.core.properties.OccurredObservationProperties;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        DataState dataState,
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
            DataState dataState,
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
        this(studyId, observationId, participantId, start, end, true, DataState.MISSING, new OccurredObservationProperties());
    }

    public enum DataState {
        MISSING("missing"),
        INCOMPLETE("incomplete"),
        PARTIAL("partial"),
        COMPLETE("complete");
        private final String value;

        DataState(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        private static final Map<String, DataState> LOOKUP;
        static {
            LOOKUP = Arrays.stream(DataState.values()).collect(Collectors.toMap(DataState::getValue, Function.identity()));
        }

        public static DataState fromValue(String value) {
            return LOOKUP.get(value);
        }
    }
}
