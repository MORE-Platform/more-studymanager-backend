package io.redlink.more.studymanager.core.sdk.schedule;

public class CronSchedule implements Schedule {
    private String cron;

    public CronSchedule(String cron) {
        this.cron = cron;
    }

    public String getCron() {
        return cron;
    }
}
