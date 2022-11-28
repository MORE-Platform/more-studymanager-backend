package io.redlink.more.studymanager.model;

public record PushNotificationsToken(
        String service,
        String token
) {
}
