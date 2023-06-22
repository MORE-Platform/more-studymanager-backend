package io.redlink.more.studymanager.core.io;

import java.time.Instant;

public class Timeframe implements TimeRange {

    private Instant from;
    private Instant to;

    public Timeframe(Instant from, Instant to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public String getFromString() {
        return from != null ? from.toString() : null;
    }

    @Override
    public String getToString() {
        return to != null ? to.toString() : null;
    }
}
