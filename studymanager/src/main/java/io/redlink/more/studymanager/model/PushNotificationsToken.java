package io.redlink.more.studymanager.model;

import java.time.Instant;

public record PushNotificationsToken(
        String service,
        String token
) {
}
