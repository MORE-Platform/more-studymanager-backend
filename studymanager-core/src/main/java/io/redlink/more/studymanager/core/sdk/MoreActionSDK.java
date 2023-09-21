package io.redlink.more.studymanager.core.sdk;


public interface MoreActionSDK extends MorePlatformSDK {
    void sendPushNotification(String title, String message);
    void triggerObservation(String title, String message, String factoryId, int observationId);
}
