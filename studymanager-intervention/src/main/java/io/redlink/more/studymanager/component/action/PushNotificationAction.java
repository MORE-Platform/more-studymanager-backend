package io.redlink.more.studymanager.component.action;

import io.redlink.more.studymanager.core.component.Action;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.properties.ActionProperties;
import io.redlink.more.studymanager.core.sdk.MoreActionSDK;

import java.lang.reflect.Parameter;

public class PushNotificationAction extends Action<ActionProperties> {

    protected PushNotificationAction(MoreActionSDK sdk, ActionProperties properties) throws ConfigurationValidationException {
        super(sdk, properties);
    }

    @Override
    public void execute(Parameter parameters) {
        sdk.sendPushNotification(properties.getString("message"));
    }
}
