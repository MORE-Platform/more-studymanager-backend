/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
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
