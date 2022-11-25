package io.redlink.more.studymanager.component.observation;

import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MorePlatformSDK;

public class PolarVerityObservationFactory<C extends Observation, P extends ObservationProperties> extends ObservationFactory<C, P> {

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
        return "Enables hart rate measurement via polar verity sensor; no properties supportet";
    }

    @Override
    public ObservationProperties validate(ObservationProperties properties) {
        return properties;
    }

    @Override
    public PolarVerityObservation create(MorePlatformSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new PolarVerityObservation(sdk, validate(properties));
    }
}
