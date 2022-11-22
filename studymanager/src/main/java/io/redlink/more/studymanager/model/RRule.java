package io.redlink.more.studymanager.model;

import java.time.Instant;

public class RRule {
    private String freq;
    private Instant until;
    private Integer count;
    private Integer interval;
    private String byDay;
    private Integer byMonth;
    private Integer byMonthDay;
    private Integer bySetPos;

    public RRule setFreq(String freq) {
        this.freq = freq;
        return this;
    }

    public String getFreq() {
        return freq;
    }

    public Instant getUntil() {
        return until;
    }

    public RRule setUntil(Instant until) {
        this.until = until;
        return this;
    }

    public Integer getCount() {
        return count;
    }

    public RRule setCount(Integer count) {
        this.count = count;
        return this;

    }

    public Integer getInterval() {
        return interval;
    }

    public RRule setInterval(Integer interval) {
        this.interval = interval;
        return this;
    }

    public String getByDay() {
        return byDay;
    }

    public RRule setByDay(String byDay) {
        this.byDay = byDay;
        return this;
    }

    public Integer getByMonth() {
        return byMonth;
    }

    public RRule setByMonth(Integer byMonth) {
        this.byMonth = byMonth;
        return this;
    }

    public Integer getByMonthDay() {
        return byMonthDay;
    }

    public RRule setByMonthDay(Integer byMonthDay) {
        this.byMonthDay = byMonthDay;
        return this;
    }

    public Integer getBySetPos() {
        return bySetPos;
    }

    public RRule setBySetPos(Integer bySetPos) {
        this.bySetPos = bySetPos;
        return this;
    }
}

