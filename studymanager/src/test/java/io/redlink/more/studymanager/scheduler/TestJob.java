/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.scheduler;

import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class TestJob  implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestJob.class);

    @Autowired
    MoreTriggerSDK moreSDK;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String issuer = jobExecutionContext.getJobDetail().getJobDataMap().getString("issuer");
        int count = moreSDK.getValue(issuer, Integer.class).orElse(0);
        LOGGER.debug("scheduled {}: count {}", issuer, count);
        moreSDK.setValue(issuer, count+1);
    }
}
