package io.redlink.more.studymanager.core.measurement;

import java.util.Set;

public record MeasurementSet(String id, Set<Measurement> values) {
    public static MeasurementSet None = new MeasurementSet("None", Set.of());

    public MeasurementSet {
        if (id == null || values == null) {
            throw new IllegalArgumentException("Is and values must not be null");
        }
    }
}
