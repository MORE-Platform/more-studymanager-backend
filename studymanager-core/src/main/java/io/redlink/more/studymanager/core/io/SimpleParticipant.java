package io.redlink.more.studymanager.core.io;

import java.time.Instant;

public class SimpleParticipant {
    private final Integer id;
    private final Instant start;

    public SimpleParticipant(Integer id, Instant start) {
        this.id = id;
        this.start = start;
    }

    public Integer getId() {
        return id;
    }

    public Instant getStart() {
        return start;
    }
}
