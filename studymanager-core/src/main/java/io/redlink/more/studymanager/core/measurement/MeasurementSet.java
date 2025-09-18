/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.core.measurement;

import java.util.Set;

public record MeasurementSet(String id, Set<Measurement> values) {
    public static MeasurementSet None = new MeasurementSet("None", Set.of());

    public MeasurementSet {
        if (id == null || values == null) {
            throw new IllegalArgumentException("Id and values must not be null");
        }
    }
}
