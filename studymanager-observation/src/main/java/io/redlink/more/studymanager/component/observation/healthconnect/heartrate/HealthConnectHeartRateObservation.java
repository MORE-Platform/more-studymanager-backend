package io.redlink.more.studymanager.component.observation.healthconnect.heartrate;

import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;

public class HealthConnectHeartRateObservation<C extends ObservationProperties> extends Observation<C> {
    protected HealthConnectHeartRateObservation(MoreObservationSDK sdk, C properties) throws ConfigurationValidationException {
        super(sdk, properties);
    }
}
