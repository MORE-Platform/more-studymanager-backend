package io.redlink.more.studymanager.service;

import com.google.firebase.messaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirebaseMessagingService {
    private final FirebaseMessaging firebaseMessaging;

    private static final Logger log = LoggerFactory.getLogger(FirebaseMessagingService.class);

    public FirebaseMessagingService(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    public void sendNotification(String title, String body, String token) throws FirebaseMessagingException {

        Notification notification = Notification
                .builder()
                .setTitle(title)
                .setBody(body)
                .build();

        ApnsConfig apnsConfig = ApnsConfig
                .builder()
                .putHeader("apns-priority", "5")
                .setAps(Aps.builder().setCategory("NEW_MESSAGE_CATEGORY").build())
                .build();

        Message message = Message
                .builder()
                .setToken(token)
                .setNotification(notification)
                .setApnsConfig(apnsConfig)
//                .putAllData()
                .build();
        if (firebaseMessaging == null) {
            log.warn("Not sending Message {}", title);
        } else {
            firebaseMessaging.send(message);
        }
    }
}
