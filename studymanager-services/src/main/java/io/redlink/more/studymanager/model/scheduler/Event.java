/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model.scheduler;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

public class Event implements ScheduleEvent {
    public static final String TYPE = "Event";
    private String type;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant dateStart;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant dateEnd;
    private RecurrenceRule recurrenceRule;

    @Override
    public String getType() {
        return TYPE;
    }

    public Instant getDateStart() {
        return dateStart;
    }

    public Event setDateStart(Instant dateStart) {
        this.dateStart = dateStart;
        return this;
    }

    public Instant getDateEnd() {
        return dateEnd;
    }

    public Event setDateEnd(Instant dateEnd) {
        this.dateEnd = dateEnd;
        return this;
    }

    public RecurrenceRule getRRule() {
        return recurrenceRule;
    }

    public Event setRRule(RecurrenceRule recurrenceRule) {
        this.recurrenceRule = recurrenceRule;
        return this;
    }


}
