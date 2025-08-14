package io.redlink.more.studymanager.component.observation;

import java.util.List;

import io.redlink.more.studymanager.component.observation.measurement.GenericMeasurementSets;
import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.measurement.MeasurementSet;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.properties.model.StringListValue;
import io.redlink.more.studymanager.core.properties.model.StringValue;
import io.redlink.more.studymanager.core.properties.model.Value;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;

public class HKStepsObservationFactory<C extends Observation<P>, P extends ObservationProperties>
        extends ObservationFactory<C, P> {

    private static List<Value> properties = List.of(
        new StringValue("question")
                .setName("TEST")
                .setDescription("TEST")
                .setRequired(true)
                .setDefaultValue("TESTTEST")
            
    );

    public List<Value> getProperties() {
        return properties;
    }

    @Override
    public String getId(){
        return "healthkit-mobile-observation:Steps_observation";
    }

    @Override
    public String getTitle(){
        return "Healthkit steps observation";
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
