package io.redlink.more.studymanager.model;

import java.time.Instant;

public class TimeRange {
    private Instant startDate;
    private Instant endDate;

    public Instant getStartDate() {
        return startDate;
    }

    public TimeRange setStartDate(Instant startDate) {
        this.startDate = startDate;
        return this;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public TimeRange setEndDate(Instant endDate) {
        this.endDate = endDate;
        return this;
    }

    @Override
    public String toString() {
        return this.startDate + ", " + this.endDate;
    }
}
