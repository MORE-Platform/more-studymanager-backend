package io.redlink.more.studymanager.event;

import io.redlink.more.studymanager.model.Participant;
import org.springframework.context.ApplicationEvent;

public class StudyParticipantClosedEvent extends ApplicationEvent {
    private Participant participant;

    public StudyParticipantClosedEvent(Object source, Participant participant) {
        super(source);
        this.participant = participant;
    }

    public Participant getParticipant() {
        return participant;
    }
}
