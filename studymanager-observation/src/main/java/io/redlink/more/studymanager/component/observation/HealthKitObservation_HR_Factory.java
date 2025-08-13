package io.redlink.more.studymanager.component.observation;



import java.util.List;
import java.util.Properties;

import io.redlink.more.studymanager.component.observation.measurement.GenericMeasurementSets;

import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;

import io.redlink.more.studymanager.core.measurement.MeasurementSet;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.properties.model.ChoiceList;
import io.redlink.more.studymanager.core.properties.model.Value;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;

public class HealthKitObservation_HR_Factory  extends ObservationFactory<HealthKitObservation, ObservationProperties> {

    private static final List<Value> PROPERTIES= List.of(
       
        
    );

   @Override
    public String getId() {
        return "healthkit-mobile-observation:HR_observation" ;
    }

    @Override
    public String getTitle() {
        return "HealthKit observation(HR)";
    }

    @Override
    public String getDescription() {
        return "Enables getting healthkit data mobile devices";
  
    }
    @Override
    public List<Value> getProperties(){
        return PROPERTIES;
    }
    @Override
    public HealthKitObservation create(MoreObservationSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new HealthKitObservation(sdk, validate(properties));
    }

   

    @Override
    public MeasurementSet getMeasurementSet(){
        return GenericMeasurementSets.HEALTHKIT_DATA;
    }
}