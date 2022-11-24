package io.redlink.more.studymanager.component.observation;

import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MorePlatformSDK;

public class GpsMobileObservationFactory<C extends Observation, P extends ObservationProperties> extends ObservationFactory<C, P> {

    @Override
    public String getId() {
        return "gps-mobile-observation";
    }

    @Override
    public String getTitle() {
        return "GPS Mobile Sensor";
    }

    @Override
    public String getDescription() {
        return
"""
Enables hart GPS data collection in mobile; Configuration:
<code>
{
    "location_interval_millis": 60000
}
</code>
""";
    }

    @Override
    public ObservationProperties validate(ObservationProperties properties) {
        return properties;
    }

    @Override
    public GpsMobileObservation create(MorePlatformSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new GpsMobileObservation(sdk, validate(properties));
    }
}
