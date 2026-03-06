package io.redlink.more.studymanager.scheduling;

import io.redlink.more.studymanager.event.StudyParticipantClosedEvent;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.service.ParticipantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StudyParticipantCron {

    private static final Logger log = LoggerFactory.getLogger(StudyParticipantCron.class);

    private final ApplicationEventPublisher applicationEventPublisher;
    private final ParticipantService participantService;

    public StudyParticipantCron(ApplicationEventPublisher applicationEventPublisher, ParticipantService participantService) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.participantService = participantService;
    }

    // every minute
    @Scheduled(cron = "0 * * * * ?")
    public void closeParticipationsForStudiesWithDurations() {
        List<Participant> participantsToClose = participantService.listParticipantsForClosing();
        log.debug("Selected {} participants to close", participantsToClose.size());
        participantsToClose.forEach(participant -> {
            log.debug("Close {} ", participant);
            try {
                publishStudyParticipantClosedEvent(participant);
                participantService.setStatus(
                        participant.getStudyId(), participant.getParticipantId(), Participant.Status.LOCKED
                );
            } catch (Exception ex) {
                log.warn("Failed to close participant {} ({} - {})", participant, ex.getClass().getSimpleName(), ex.getMessage(), ex);
            }
        });
    }

    private void publishStudyParticipantClosedEvent(final Participant participant) {
        applicationEventPublisher.publishEvent(new StudyParticipantClosedEvent(this, participant));
    }


}
