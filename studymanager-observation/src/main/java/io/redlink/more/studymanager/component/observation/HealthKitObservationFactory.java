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

    private static final List<Value> PROPERTIES= List.of(
        new StringValue("ObservationType")
        .setDefaultValue("StepsRecord")
        .setRequired(true)
        .setName("Observation Type")
        .setDescription("This is the type of observation we are getting data from")
    );

   @Override
    public String getId() {
        return "healthkit-mobile-observation";
    }

    @Override
    public String getTitle() {
        return "HealthKit observation(Android)";
    }

    @Override
    public String getDescription() {
        return "Enables getting healthkit data from android devices";
  
    }
    @Override
    public List<Value> getProperties(){
        return PROPERTIES;
    }
    @Override
    public HealthKitObservation create(MoreObservationSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new HealthKitObservation(sdk, validate((P)properties));
    }

   

    @Override
    public MeasurementSet getMeasurementSet(){
        return GenericMeasurementSets.HEALTHKIT_DATA;
    }
}
