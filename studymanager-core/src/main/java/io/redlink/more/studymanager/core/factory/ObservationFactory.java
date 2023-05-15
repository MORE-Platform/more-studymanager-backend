package io.redlink.more.studymanager.core.factory;

import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;

public abstract class ObservationFactory<C extends Observation<P>, P extends ObservationProperties> extends ComponentFactory<C, P> {
    public abstract C create(MoreObservationSDK sdk, P properties) throws ConfigurationValidationException;

    @Override
    public Class<ObservationProperties> getPropertyClass() {
        return ObservationProperties.class;
    }

}
