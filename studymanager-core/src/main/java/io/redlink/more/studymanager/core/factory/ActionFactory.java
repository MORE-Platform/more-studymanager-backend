package io.redlink.more.studymanager.core.factory;

import io.redlink.more.studymanager.core.component.Action;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.properties.ActionProperties;
import io.redlink.more.studymanager.core.sdk.MoreActionSDK;

public abstract class ActionFactory<C extends Action, P extends ActionProperties> extends ComponentFactory<C, P> {
    public abstract C create(MoreActionSDK sdk, P properties) throws ConfigurationValidationException;
}
