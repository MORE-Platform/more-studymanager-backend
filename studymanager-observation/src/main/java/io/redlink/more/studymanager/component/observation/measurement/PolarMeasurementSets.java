package io.redlink.more.studymanager.component.observation.measurement;

import java.util.Set;

import io.redlink.more.studymanager.core.measurement.Measurement;
import io.redlink.more.studymanager.core.measurement.MeasurementSet;

public class PolarMeasurementSets {
    private PolarMeasurementSets(){

    }
    public static MeasurementSet PPI = new MeasurementSet(
        "data" , Set.of(
            new Measurement("null", null)
        )
    )
    
}
