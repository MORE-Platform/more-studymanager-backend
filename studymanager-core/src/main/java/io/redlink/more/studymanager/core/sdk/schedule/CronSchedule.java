package io.redlink.more.studymanager.core.sdk.schedule;

public class CronSchedule implements Schedule {
    private String cronExpression;

    public CronSchedule(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getCronExpression() {
        return cronExpression;
    }
}
