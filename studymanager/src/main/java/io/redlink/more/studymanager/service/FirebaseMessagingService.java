package io.redlink.more.studymanager.service;

import com.google.firebase.messaging.*;
import io.redlink.more.studymanager.utils.MapperUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    private enum apnsPushType {
        ALERT("alert"),
        BACKGROUND("background");

        public final String value;

        apnsPushType(String value) {
            this.value = value;
        }
    }
    private static final String apnsPriorityHeader = "apns-priority";
    private static final String apsCategory = "NEW_MESSAGE_CATEGORY";
    private static final String apsDataCategory = "DATA_CATEGORY";
    private static final String apnsPushTypeHeader = "apns-push-type";

    private static final Logger log = LoggerFactory.getLogger(FirebaseMessagingService.class);

    public FirebaseMessagingService(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    public String sendNotification(String title, String body, String token) throws FirebaseMessagingException {

        String uuid = UUID.randomUUID().toString();
        Map<String,String> data = Map.of("MSG_ID", uuid);

        Notification notification = Notification
                .builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message
                .builder()
                .putAllData(data)
                .setToken(token)
                .setNotification(notification)
                .setApnsConfig(getApnsConfig(apsCategory, apnsPushType.ALERT, apnsPriority.MEDIUM))
                .build();
        if (firebaseMessaging == null) {
            log.warn("Not sending Message {}", title);
        } else {
            final String msgId = firebaseMessaging.send(message);
            log.debug("Successfully sent FCM Message {} ({}:{})", title, msgId, uuid);
            return uuid;
        }
        return null;
    }

    private static ApnsConfig getApnsConfig(String apsCategory, apnsPushType type, apnsPriority priority) {
        return ApnsConfig
                .builder()
                .putHeader(apnsPriorityHeader, String.valueOf(priority.value))
                .putHeader(apnsPushTypeHeader, type.value)
                .setAps(Aps.builder().setCategory(apsCategory).build())
                .build();
    }

    public String sendDataNotification(Map<String, String> data, String token) throws FirebaseMessagingException {
        String uuid = UUID.randomUUID().toString();
        Map<String,String> dataUp = new HashMap<>(data);
        dataUp.put("MSG_ID", uuid);

        Message message = Message
                .builder()
                .setToken(token)
                .setApnsConfig(getApnsConfig(apsDataCategory, apnsPushType.BACKGROUND, apnsPriority.MEDIUM))
                .putAllData(dataUp)
                .build();

        if (firebaseMessaging == null) {
            log.warn("Not sending data Message {}", MapperUtils.writeValueAsString(data));
        } else {
            final String msgId = firebaseMessaging.send(message);
            log.debug("Successfully sent FCM data Message {}:{}", msgId, uuid);
            return uuid;
        }
        return null;
    }
}
