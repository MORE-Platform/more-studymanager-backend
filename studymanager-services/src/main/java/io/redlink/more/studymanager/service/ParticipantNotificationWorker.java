package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.event.StudyParticipantClosedEvent;
import io.redlink.more.studymanager.event.StudyStateChangedEvent;
import io.redlink.more.studymanager.model.Study;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ParticipantNotificationWorker {

    private static final Logger log = LoggerFactory.getLogger(ParticipantNotificationWorker.class);
    private final ParticipantService participantService;
    private final PushNotificationService pushNotificationService;

    public ParticipantNotificationWorker(
            ParticipantService participantService,
            PushNotificationService pushNotificationService) {
        this.participantService = participantService;
        this.pushNotificationService = pushNotificationService;
    }

    @EventListener
    public void handleStudyStateChanged(StudyStateChangedEvent event) {
        log.info("Notifying participants of Study[id:{}] about state transition from {} to {}",
                event.getStudy().getStudyId(), event.getPreviousState(), event.getStudy().getStudyState());
        participantService.listParticipants(event.getStudy().getStudyId())
            .forEach(participant ->
                pushNotificationService.sendStudyStateUpdate(
                    participant,
                    event.getPreviousState(),
                    event.getStudy().getStudyState()));
    }

    @EventListener
    public void handleStudyParticipantClosed(StudyParticipantClosedEvent event) {
        log.info("Notifying participant[study={}, id={}] about transition to closed state",
                event.getParticipant().getStudyId(), event.getParticipant().getParticipantId());
        pushNotificationService.sendStudyStateUpdate(
                event.getParticipant(), Study.Status.ACTIVE, Study.Status.CLOSED
        );
    }
}
