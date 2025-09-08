package io.redlink.more.studymanager.component.observation;

import java.util.List;

import io.redlink.more.studymanager.component.observation.measurement.GenericMeasurementSets;
import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.measurement.MeasurementSet;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.properties.model.IntegerValue;
import io.redlink.more.studymanager.core.properties.model.Value;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;

public class Polar360ObservationFactory <C extends Observation<P>, P extends ObservationProperties> extends ObservationFactory<C, P>  {
    @Override
    public String getId() {
        return "polar360observation";
    }


    private static List<Value> properties = List.of(
        
    ); 


    @Override
    public String getTitle() {
        return "Polar 360";
    }

    @Override
    public String getDescription() {
        return "Test ";
    }
    public List<Value> getProperties() {
        return properties;
    }
    @Override
    public Polar360Observation create(MoreObservationSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new Polar360Observation(sdk, validate((P)properties));
    }

    @Override
    public MeasurementSet getMeasurementSet() {
        return GenericMeasurementSets.NOT_SPECIFIED;
    }
}
