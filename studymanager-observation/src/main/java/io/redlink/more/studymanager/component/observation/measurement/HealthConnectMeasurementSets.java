package io.redlink.more.studymanager.component.observation.measurement;

import io.redlink.more.studymanager.core.measurement.Measurement;
import io.redlink.more.studymanager.core.measurement.MeasurementSet;

import java.util.Set;

public class HealthConnectMeasurementSets {

    private HealthConnectMeasurementSets() {
    }

    public static MeasurementSet STEPS = new MeasurementSet(
            "STEPS", Set.of(
            new Measurement("steps", Measurement.Type.INTEGER)
    ));
}
