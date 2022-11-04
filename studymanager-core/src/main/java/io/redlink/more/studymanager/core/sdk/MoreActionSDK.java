package io.redlink.more.studymanager.core.sdk;

import io.redlink.more.studymanager.core.component.Action;

public interface MoreActionSDK extends MorePlatformSDK {
    void setAction(Action action);
    void sendPushNotification(String message);
}
