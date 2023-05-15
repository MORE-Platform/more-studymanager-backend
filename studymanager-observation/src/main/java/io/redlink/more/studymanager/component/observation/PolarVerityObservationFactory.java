package io.redlink.more.studymanager.component.observation;

import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;

public class PolarVerityObservationFactory<C extends Observation<P>, P extends ObservationProperties> extends ObservationFactory<C, P> {

    @Override
    public String getId() {
        return "polar-verity-observation";
    }

    @Override
    public String getTitle() {
        return "Polar Verity Sensor";
    }

    @Override
    public String getDescription() {
        return "This observation enables you to collect heart rate data via the polar verity sensor. No properties are supported.";
    }

    @Override
    public ObservationProperties validate(ObservationProperties properties) {
        return properties;
    }

    @Override
    public PolarVerityObservation create(MoreObservationSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new PolarVerityObservation(sdk, validate(properties));
    }
}
