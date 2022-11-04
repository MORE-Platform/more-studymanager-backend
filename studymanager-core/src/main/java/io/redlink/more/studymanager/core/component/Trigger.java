package io.redlink.more.studymanager.core.component;

import io.redlink.more.studymanager.core.Parameters;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.properties.TriggerProperties;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;

public abstract class Trigger<C extends TriggerProperties> extends Component<C> {

    protected final MoreTriggerSDK sdk;

    protected Trigger(MoreTriggerSDK sdk, C properties) throws ConfigurationValidationException {
        super(properties);
        this.sdk = sdk;
    }

    public abstract Parameters execute(Parameters parameters);


}
