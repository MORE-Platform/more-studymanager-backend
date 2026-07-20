package io.redlink.more.studymanager.event;

import io.redlink.more.studymanager.model.Study;
import org.springframework.context.ApplicationEvent;

public class StudyStateChangedEvent extends ApplicationEvent {
    private Study study;
    private Study.Status previousState;

    public StudyStateChangedEvent(Object source, Study study, Study.Status previousState) {
        super(source);
        this.study = study;
        this.previousState = previousState;
    }

    public Study getStudy() {
        return study;
    }

    public Study.Status getPreviousState() {
        return previousState;
    }
}
