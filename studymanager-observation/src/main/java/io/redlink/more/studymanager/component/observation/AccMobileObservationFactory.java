package io.redlink.more.studymanager.component.observation;

import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MorePlatformSDK;

public class AccMobileObservationFactory<C extends Observation, P extends ObservationProperties>
        extends ObservationFactory<C, P> {
    @Override
    public String getId() {
        return "acc-mobile-observation";
    }

    @Override
    public String getTitle() {
        return "Accelerometer Mobile";
    }

    @Override
    public String getDescription() {
        return """
                This observation enables you to collect accelerometer data via the mobile sensor.
                """;
    }

    @Override
    public ObservationProperties validate(ObservationProperties properties) {
        return properties;
    }

    @Override
    public AccMobileObservation create(MorePlatformSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new AccMobileObservation(sdk, validate(properties));
    }
}
