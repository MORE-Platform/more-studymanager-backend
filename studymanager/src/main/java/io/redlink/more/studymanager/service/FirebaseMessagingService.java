package io.redlink.more.studymanager.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
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

        Message message = Message
                .builder()
                .setToken(token)
                .setNotification(notification)
//                .putAllData()
                .build();
        if (firebaseMessaging == null) {
            log.warn("Not sending Message {}", title);
        } else {
            firebaseMessaging.send(message);
        }
    }
}
