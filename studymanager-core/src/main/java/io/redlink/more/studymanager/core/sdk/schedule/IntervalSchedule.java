package io.redlink.more.studymanager.core.sdk.schedule;

public class IntervalSchedule implements Schedule {
    private long delay;
    private long period;

    public IntervalSchedule(long delay, long period) {
        this.delay = delay;
        this.period = period;
    }

    public long getDelay() {
        return delay;
    }

    public long getPeriod() {
        return period;
    }
}
