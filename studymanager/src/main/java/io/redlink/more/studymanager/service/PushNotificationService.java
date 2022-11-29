package io.redlink.more.studymanager.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import io.redlink.more.studymanager.model.PushNotificationsToken;
import io.redlink.more.studymanager.repository.PushNotificationTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PushNotificationService {
    private final PushNotificationTokenRepository pushNotificationsRepository;
    private final FirebaseMessagingService firebaseService;

    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

    public PushNotificationService(PushNotificationTokenRepository pushNotificationsRepository,
                                   FirebaseMessagingService firebaseService) {
        this.pushNotificationsRepository = pushNotificationsRepository;
        this.firebaseService = firebaseService;
    }

    public boolean sendPushNotification(long studyID, int participantId, String title, String message) {
        Optional<PushNotificationsToken> tkn = this.pushNotificationsRepository.getTokenById(studyID, (participantId));

        if (tkn.isPresent()) {
            PushNotificationsToken token = tkn.get();
            if (token.service().equals("FCM")) {
                try {
                    this.firebaseService.sendNotification(title, message, token.token());
                    return true;
                } catch (FirebaseMessagingException e) {
                    log.warn("Could not send Notification: {}", e.getMessage(), e);
                    return false;
                }
            }
        } else {
            log.warn("Could not send notification: missing token for participant sid:{} pid:{}", studyID, participantId);
        }

        return false;
    }
}
