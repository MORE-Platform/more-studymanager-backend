package io.redlink.more.studymanager.core.component;

import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MorePlatformSDK;

public abstract class Observation<C extends ObservationProperties> extends Component<C> {

    protected final MorePlatformSDK sdk;
    protected Observation(MorePlatformSDK sdk, C properties) throws ConfigurationValidationException {
        super(properties);
        this.sdk = sdk;
    }
}
