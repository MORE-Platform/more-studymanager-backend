package io.redlink.more.studymanager.component.observation;

import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MorePlatformSDK;

public class LimeSurveyObservation<C extends ObservationProperties> extends Observation<C> {

    public LimeSurveyObservation(MorePlatformSDK sdk, C properties) throws ConfigurationValidationException {
        super(sdk, properties);
    }
}