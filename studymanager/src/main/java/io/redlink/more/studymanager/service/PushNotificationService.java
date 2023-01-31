package io.redlink.more.studymanager.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import io.redlink.more.studymanager.model.PushNotificationsToken;
import io.redlink.more.studymanager.repository.PushNotificationTokenRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PushNotificationService {
    private final PushNotificationTokenRepository pushNotificationsRepository;
    private final FirebaseMessagingService firebaseService;

    private static final Logger LOG = LoggerFactory.getLogger(PushNotificationService.class);

    public PushNotificationService(PushNotificationTokenRepository pushNotificationsRepository,
                                   FirebaseMessagingService firebaseService) {
        this.pushNotificationsRepository = pushNotificationsRepository;
        this.firebaseService = firebaseService;
    }

    public boolean sendPushNotification(long studyID, int participantId, String title, String message) {
        Optional<PushNotificationsToken> tkn = this.pushNotificationsRepository.getTokenById(studyID, participantId);

        if (tkn.isEmpty()) {
            LOG.debug("Can't send notification: No token for participant sid:{} pid:{} registered", studyID, participantId);
            return false;
        }

        final PushNotificationsToken token = tkn.get();
        final String serviceType = token.service();
        if ("FCM".equals(serviceType)) {
            try {
                this.firebaseService.sendNotification(title, message, token.token());
                return true;
            } catch (FirebaseMessagingException e) {
                if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                    LOG.debug("Outdated FCM-Notification token (sid:{} pid:{}), trying to clean-up...", studyID, participantId);
                    if (pushNotificationsRepository.deleteToken(studyID, participantId)) {
                        LOG.info("Cleared outdated FCM-Notification token (sid:{} pid:{})", studyID, participantId);
                    }
                } else {
                    LOG.warn("Could not send Notification (sid:{} pid:{}): {}", studyID, participantId, e.getMessage(), e);
                }
                return false;
            }
        } else {
            LOG.warn("Unknown Notification-Service-Type: {} - cannot send notification (sid:{} pid:{})",
                    serviceType, studyID, participantId);
            return false;
        }
    }
}
