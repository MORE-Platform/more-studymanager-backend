package io.redlink.more.studymanager.component.action;

import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ActionFactory;
import io.redlink.more.studymanager.core.properties.ActionProperties;
import io.redlink.more.studymanager.core.sdk.MoreActionSDK;
import io.redlink.more.studymanager.core.validation.ConfigurationValidationReport;

import java.util.Map;

public class PushNotificationActionFactory extends ActionFactory<PushNotificationAction, ActionProperties> {
    @Override
    public PushNotificationAction create(MoreActionSDK sdk, ActionProperties properties) throws ConfigurationValidationException {
        return new PushNotificationAction(sdk, validate(properties));
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
        return
"""
Sends a push notification to all matching participants based on trigger. Example: <code>
{
   "title": "Hello World",
   "message": "Nice to greet you"
}
</code>
""";
    }

    @Override
    public Map<String, Object> getDefaultProperties() {
        return Map.of(
                "title", "Hello World",
                "message", "Nice to greet you"
        );
    }

    @Override
    public ActionProperties validate(ActionProperties properties) {
        ConfigurationValidationReport report = ConfigurationValidationReport.init();
        if(!properties.containsKey("title")) {
            report.missingProperty("title");
        }
        if(!properties.containsKey("message")) {
            report.missingProperty("message");
        }
        if(report.isValid()) {
            return properties;
        } else {
            throw new ConfigurationValidationException(report);
        }
    }
}
