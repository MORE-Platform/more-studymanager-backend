package io.redlink.more.studymanager.core.factory;

import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.properties.ComponentProperties;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MorePlatformSDK;

public abstract class ObservationFactory<C extends Observation, P extends ObservationProperties> extends ComponentFactory<C, P> {
    public abstract C create(MorePlatformSDK sdk, P properties) throws ConfigurationValidationException;

    @Override
    public Class<ObservationProperties> getPropertyClass() {
        return ObservationProperties.class;
    }
}
