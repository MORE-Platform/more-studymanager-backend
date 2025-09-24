package io.redlink.more.studymanager.component.observation;

import java.util.List;

import io.redlink.more.studymanager.component.observation.measurement.GenericMeasurementSets;
import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.measurement.MeasurementSet;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.properties.model.BooleanValue;
import io.redlink.more.studymanager.core.properties.model.IntegerValue;
import io.redlink.more.studymanager.core.properties.model.StringValue;
import io.redlink.more.studymanager.core.properties.model.Value;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;

public class HKHRObservationFactory <C extends Observation<P>, P extends ObservationProperties>
        extends ObservationFactory<C, P> {
    

             private static List<Value> properties = List.of(
        new IntegerValue("daysback").setDefaultValue(1).setDescription("Data collection days back from current date"),
        new BooleanValue("sendRawData").setDefaultValue(false).setDescription(
                "Fetching fully raw data from devices with not cleaning"
            )
      
            
    );

    public List<Value> getProperties() {
        return properties;
    }

    @Override
    public String getId(){
        return "healthkit-mobile-observation:HR_observation";
    }

    @Override
    public String getTitle(){
        return "Healthkit HR observation";
    }

     @Override
    public String getDescription() {
        return """
                Healthkit steps record collection
                """;
    }

    @Override
    public HkObservation create(MoreObservationSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
         return new HkObservation(sdk, validate((P)properties));
    }


    @Override
    public MeasurementSet getMeasurementSet() {
        return GenericMeasurementSets.NOT_SPECIFIED;
    }

}
