package io.redlink.more.studymanager.core.io;

public class RelativeTimeFrame implements TimeRange {

    private long timeInSec;

    public RelativeTimeFrame(long timeInSec) {
        this.timeInSec = timeInSec;
    }

    @Override
    public String getFromString() {
        return "now-" + timeInSec + "s";
    }

    @Override
    public String getToString() {
        return "now";
    }
}
