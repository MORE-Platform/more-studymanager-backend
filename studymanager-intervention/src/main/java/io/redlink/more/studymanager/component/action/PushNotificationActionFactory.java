package io.redlink.more.studymanager.component.action;

import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ActionFactory;
import io.redlink.more.studymanager.core.sdk.MoreActionSDK;
import io.redlink.more.studymanager.core.validation.ConfigurationValidationReport;

public class PushNotificationActionFactory extends ActionFactory<PushNotificationAction, PushNotificationActionProperties> {
    @Override
    public PushNotificationAction create(MoreActionSDK sdk, PushNotificationActionProperties properties) throws ConfigurationValidationException {
        return null;
    }

    @Override
    public String getId() {
        return "push-notification-action";
    }

    @Override
    public String getTitle() {
        return "Push Notification Action";
    }

    @Override
    public String getDescription() {
        return "Sends a push notification to all matching participants based on trigger";
    }

    @Override
    public PushNotificationActionProperties validate(PushNotificationActionProperties properties) {
        ConfigurationValidationReport report = ConfigurationValidationReport.init();
        if(properties.getMessage().isEmpty()) {
            report.missingProperty("message");
        }
        if(report.isValid()) {
            return properties;
        } else {
            throw new ConfigurationValidationException(report);
        }
    }
}
