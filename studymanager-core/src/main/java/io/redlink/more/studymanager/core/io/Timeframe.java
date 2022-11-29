package io.redlink.more.studymanager.core.io;

import java.time.Instant;

public class Timeframe {

    private Instant from;
    private Instant to;

    public Timeframe(Instant from, Instant to) {
        this.from = from;
        this.to = to;
    }

    public Instant getFrom() {
        return from;
    }

    public Instant getTo() {
        return to;
    }
}
