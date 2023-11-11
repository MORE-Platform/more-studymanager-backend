/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.component.observation.measurement;

import io.redlink.more.studymanager.core.measurement.Measurement;
import io.redlink.more.studymanager.core.measurement.MeasurementSet;

import java.util.Set;

public class GenericMeasurementSets {

    private GenericMeasurementSets() {
    }

    public static MeasurementSet ACCELEROMETER = new MeasurementSet(
            "ACCELEROMETER", Set.of(
                    new Measurement("x", Measurement.Type.DOUBLE),
                    new Measurement("y", Measurement.Type.DOUBLE),
                    new Measurement("z", Measurement.Type.DOUBLE))
    );

    public static MeasurementSet GEOLOCATION = new MeasurementSet(
            "GEOLOCATION", Set.of(
            new Measurement("latitude", Measurement.Type.DOUBLE),
            new Measurement("longitude", Measurement.Type.DOUBLE),
            new Measurement("altitude", Measurement.Type.DOUBLE))
    );

    public static MeasurementSet HEART_RATE = new MeasurementSet("HEART_RATE", Set.of(
            new Measurement("hr", Measurement.Type.INTEGER)
    ));

    public static MeasurementSet NOT_SPECIFIED = new MeasurementSet("NOT_SPECIFIED", Set.of(
            Measurement.Any
    ));

}
