package io.redlink.more.studymanager.component.observation;


import java.util.List;
import java.util.Set;

import io.redlink.more.studymanager.component.observation.measurement.GenericMeasurementSets;
import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.measurement.Measurement;
import io.redlink.more.studymanager.core.measurement.MeasurementSet;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.properties.model.StringValue;
import io.redlink.more.studymanager.core.properties.model.Value;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;


public class HealthKitObservationFactory <C extends Observation<P>, P extends ObservationProperties> extends ObservationFactory<C, P> {
    
   @Override
    public String getId() {
        return "healthkit-mobile-observation";
    }

    @Override
    public String getTitle() {
        return "observation.factory.Healthkit.title";
    }

    @Override
    public String getDescription() {
        return
"""
observation.factory.Healthkit.description
""";
    }
    @Override
    public HealthKitObservation create(MoreObservationSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new HealthKitObservation(sdk, validate((P)properties));
    }

   
    //public List<MeasurementSet> getMeasurementSetList() {
    //    return List.of(
    //        GenericMeasurementSets.STEPS,
    //        GenericMeasurementSets.HEART_RATE
    //    );
    //}

    @Override
    public MeasurementSet getMeasurementSet(){
        return GenericMeasurementSets.STEPS;
    }
}
