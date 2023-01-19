package io.redlink.more.studymanager.scheduling;

import io.redlink.more.studymanager.core.exception.SchedulingException;
import io.redlink.more.studymanager.core.sdk.schedule.CronSchedule;
import io.redlink.more.studymanager.core.sdk.schedule.Schedule;
import org.apache.commons.lang3.NotImplementedException;
import org.quartz.*;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Service
public class SchedulingService {

    public static final String TRIGGER = "trigger";
    public static final String JOB = "job";
    private final Scheduler scheduler;

    public SchedulingService(SchedulerFactoryBean factory) throws SchedulerException {
        this.scheduler = factory.getScheduler();
        this.scheduler.start();
    }

    public <T extends Job> String scheduleJob(String issuer, Map<String,Object> data, Schedule schedule, Class<T> type) {
        String id = UUID.randomUUID().toString();

        final JobDetail job = newJob().ofType(type)
                .withIdentity(new JobKey(getInnerId(JOB, issuer, id), type.getSimpleName()))
                .usingJobData(new JobDataMap(data))
                .build();

        final Trigger trigger = newTrigger().forJob(job)
                .withIdentity(new TriggerKey(getInnerId(TRIGGER, issuer, id), type.getSimpleName()))
                .withSchedule(getSchedulerBuilderFor(schedule))
                .build();

        try {
            this.scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            throw new SchedulingException(e);
        }

        return id;
    }

    public <T extends Job> void unscheduleJob(String issuer, String id, Class<T> type) {
        try {
            this.scheduler.unscheduleJob(new TriggerKey(getInnerId(TRIGGER, issuer, id), type.getSimpleName()));
            this.scheduler.deleteJob(new JobKey(getInnerId(JOB, issuer, id), type.getSimpleName()));
        } catch (SchedulerException e) {
            throw new SchedulingException(e);
        }
    }

    @PreDestroy
    public void preDestroy() throws SchedulerException {
        if(this.scheduler != null) {
            scheduler.shutdown();
        }
    }

    private ScheduleBuilder<? extends Trigger> getSchedulerBuilderFor(Schedule schedule) {
        if(schedule instanceof CronSchedule) {
            return CronScheduleBuilder.cronSchedule(((CronSchedule) schedule).getCronExpression())
                    .inTimeZone(TimeZone.getTimeZone("Europe/Vienna")); //TODO make configurable per study
        } else {
            throw new NotImplementedException("SchedulerType " + schedule.getClass().getSimpleName() + " not yet supportet");
        }
    }

    private String getInnerId(String prefix, String issuer, String id) {
        return prefix + "-" + issuer + "-" + id;
    }


}
