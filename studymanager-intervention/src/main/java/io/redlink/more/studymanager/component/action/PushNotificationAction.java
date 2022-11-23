package io.redlink.more.studymanager.component.action;

import io.redlink.more.studymanager.core.component.Action;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.sdk.MoreActionSDK;

import java.lang.reflect.Parameter;

public class PushNotificationAction extends Action<PushNotificationActionProperties> {

    protected PushNotificationAction(MoreActionSDK sdk, PushNotificationActionProperties properties) throws ConfigurationValidationException {
        super(sdk, properties);
    }

    @Override
    public void execute(Parameter parameters) {
        properties.getMessage().ifPresent(sdk::sendPushNotification);
    }
}
