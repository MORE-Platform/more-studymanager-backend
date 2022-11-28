package io.redlink.more.studymanager.core.component;

import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.io.ActionParameter;
import io.redlink.more.studymanager.core.properties.ActionProperties;
import io.redlink.more.studymanager.core.sdk.MoreActionSDK;

public abstract class Action<C extends ActionProperties> extends Component<C> {
    protected final MoreActionSDK sdk;
    protected Action(MoreActionSDK sdk, C properties) throws ConfigurationValidationException {
        super(properties);
        this.sdk = sdk;
    }

    public abstract void execute(ActionParameter parameter);


    @Override
    public void activate() {
        // no action
    }

    @Override
    public void deactivate() {
        // no action
    }
}
