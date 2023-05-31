package io.redlink.more.studymanager.core.factory;

import io.redlink.more.studymanager.core.component.Action;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.properties.ActionProperties;
import io.redlink.more.studymanager.core.properties.ComponentProperties;
import io.redlink.more.studymanager.core.properties.model.Value;
import io.redlink.more.studymanager.core.sdk.MoreActionSDK;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ActionFactory<C extends Action, P extends ActionProperties> extends ComponentFactory<C, P> {
    public abstract C create(MoreActionSDK sdk, P properties) throws ConfigurationValidationException;

    @Override
    public Class<ActionProperties> getPropertyClass() {
        return ActionProperties.class;
    }
}
