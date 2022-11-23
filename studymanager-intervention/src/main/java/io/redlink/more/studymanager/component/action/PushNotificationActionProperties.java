package io.redlink.more.studymanager.component.action;

import io.redlink.more.studymanager.core.properties.ActionProperties;

import java.util.Optional;

public class PushNotificationActionProperties extends ActionProperties {
    public Optional<String> getMessage() {
        return Optional.ofNullable(this.getString("message"));
    }
}
