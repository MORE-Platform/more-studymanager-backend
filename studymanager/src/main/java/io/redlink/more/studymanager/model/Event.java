package io.redlink.more.studymanager.model;

import java.time.Instant;

public class Event {
    private Instant dateStart;
    private Instant dateEnd;
    private RRule rRule;

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

    public RRule getRRule() {
        return rRule;
    }

    public Event setRRule(RRule rRule) {
        this.rRule = rRule;
        return this;
    }
}
