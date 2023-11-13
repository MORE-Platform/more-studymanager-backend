/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.scheduler;

import io.redlink.more.studymanager.sdk.MoreSDK;
import org.junit.jupiter.api.Test;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test-containers-flyway")
public class TestQrtzScheduler {

    @MockBean
    private MoreSDK moreSDK;

    @Autowired
    private SchedulerFactoryBean factory;

    @Test
    public void testScheduling() throws SchedulerException, InterruptedException {

        Map<String, AtomicInteger> store = new HashMap<>();

        when(moreSDK.getValue(anyString(), anyString(), any())).thenAnswer(in -> {
            String key = in.getArgument(0) + "-" + in.getArgument(1);
            return Optional.of(store.computeIfAbsent(key, s -> new AtomicInteger()).incrementAndGet());
        });

        Scheduler scheduler = factory.getScheduler();
        JobDetail job1 = jobDetail("1");
        JobDetail job2 = jobDetail("2");
        scheduler.start();

        Trigger t1 = trigger(job1,"1");
        System.out.println();

        scheduler.scheduleJob(job1, t1);

        TimeUnit.MILLISECONDS.sleep(1000);

        Trigger t2 = trigger(job2, "2");
        scheduler.scheduleJob(job2, t2);

        TimeUnit.MILLISECONDS.sleep(1000);
        scheduler.unscheduleJob(t1.getKey());
        scheduler.deleteJob(job1.getKey());

        assertThat(store.get("issuer1-i").get()).isGreaterThanOrEqualTo(7);
        assertThat(store.get("issuer2-i").get()).isGreaterThanOrEqualTo(4);

        TimeUnit.MILLISECONDS.sleep(1200);
        scheduler.unscheduleJob(t2.getKey());
        scheduler.deleteJob(job2.getKey());

        assertThat(store.get("issuer2-i").get()).isEqualTo(8);
    }

    private JobDetail jobDetail(String id) {
        return newJob().ofType(TestJob.class)
                .storeDurably()
                .withIdentity(id)
                .usingJobData("issuer", "issuer"+id )
                .build();
    }

    @Test
    public void testTriggerStopWithStringKey() throws SchedulerException, InterruptedException {
        Scheduler scheduler = factory.getScheduler();
        scheduler.start();
        JobDetail job = jobDetail("job");
        String triggerId = "trigger";
        scheduler.scheduleJob(job, trigger(job, triggerId));
        TimeUnit.MILLISECONDS.sleep(500);
        scheduler.unscheduleJob(new TriggerKey(triggerId));
        scheduler.deleteJob(job.getKey());
        verify(moreSDK, atLeast(2)).getValue(any(),any(),any());
        reset(moreSDK);
        TimeUnit.MILLISECONDS.sleep(200);
        verify(moreSDK, never()).getValue(any(),any(),any());
        scheduler.shutdown();
    }

    public Trigger trigger(JobDetail job, String key) {
        int frequencyInMillis = 300;
        return newTrigger().forJob(job)
                .withIdentity(key)
                .withSchedule(
                        simpleSchedule().withIntervalInMilliseconds(frequencyInMillis).repeatForever()
                ).build();
    }

}
