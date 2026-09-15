package io.redlink.more.studymanager.event;

import io.redlink.more.studymanager.model.Participant;
import org.springframework.context.ApplicationEvent;

public class ParticipantMilestoneChangedEvent extends ApplicationEvent {
    private final Participant participant;

    public ParticipantMilestoneChangedEvent(Object source, Participant participant) {
        super(source);
        this.participant = participant;
    }

    public Participant getParticipant() {
        return participant;
    }
}
