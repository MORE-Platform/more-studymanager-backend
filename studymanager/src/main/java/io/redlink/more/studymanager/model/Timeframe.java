package io.redlink.more.studymanager.model;

import java.time.LocalDate;

public class Timeframe {
    private LocalDate from;
    private LocalDate to;


    public LocalDate getFrom() {
        return from;
    }

    public Timeframe setFrom(LocalDate from) {
        this.from = from;
        return this;
    }

    public LocalDate getTo() {
        return to;
    }

    public Timeframe setTo(LocalDate to) {
        this.to = to;
        return this;
    }
}
