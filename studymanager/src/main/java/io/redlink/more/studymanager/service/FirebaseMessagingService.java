package io.redlink.more.studymanager.service;

import com.google.firebase.messaging.*;
import io.redlink.more.studymanager.utils.MapperUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class FirebaseMessagingService {
    private final FirebaseMessaging firebaseMessaging;
    private enum apnsPriority{
        LOW(1),
        MEDIUM(5),
        HIGH(10);

        public final int value;
        apnsPriority(int value) {
            this.value = value;
        }
    }
    private static final String apnsPriorityHeader = "apns-priority";
    private static final String apsCategory = "NEW_MESSAGE_CATEGORY";
    private static final String apsDataCategory = "DATA_CATEGORY";

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
                .setApnsConfig(getApnsConfig(apsCategory))
                .build();
        if (firebaseMessaging == null) {
            log.warn("Not sending Message {}", title);
        } else {
            final String msgId = firebaseMessaging.send(message);
            log.debug("Successfully sent FCM Message {} ({})", title, msgId);
        }
    }

    private static ApnsConfig getApnsConfig(String apsCategory) {
        return ApnsConfig
                .builder()
                .putHeader(apnsPriorityHeader, String.valueOf(apnsPriority.MEDIUM.value))
                .setAps(Aps.builder().setCategory(apsCategory).build())
                .build();
    }

    public void sendDataNotification(Map<String, String> data, String token) throws FirebaseMessagingException {
        Message message = Message
                .builder()
                .setToken(token)
                .setApnsConfig(getApnsConfig(apsDataCategory))
                .putAllData(data)
                .build();

        if (firebaseMessaging == null) {
            log.warn("Not sending data Message {}", MapperUtils.writeValueAsString(data));
        } else {
            final String msgId = firebaseMessaging.send(message);
            log.debug("Successfully sent FCM Message {}", msgId);
        }
    }
}
