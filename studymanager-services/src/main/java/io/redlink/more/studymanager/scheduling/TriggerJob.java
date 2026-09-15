/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.scheduling;

import io.redlink.more.studymanager.action.ActionService;
import io.redlink.more.studymanager.core.exception.SchedulingException;
import io.redlink.more.studymanager.core.factory.TriggerFactory;
import io.redlink.more.studymanager.core.io.Parameters;
import io.redlink.more.studymanager.core.io.TriggerResult;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.model.Intervention;
import io.redlink.more.studymanager.model.Trigger;
import io.redlink.more.studymanager.sdk.MoreSDK;
import io.redlink.more.studymanager.service.InterventionService;
import io.redlink.more.studymanager.utils.LoggingUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class TriggerJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(TriggerJob.class);

    private final MoreSDK moreSDK;
    private final ApplicationContext applicationContext;
    private final InterventionService interventionService;
    private final ActionService actionService;

    public TriggerJob(
            MoreSDK moreSDK,
            InterventionService interventionService,
            ActionService actionService,
            ApplicationContext applicationContext) {
        this.moreSDK = moreSDK;
        this.applicationContext = applicationContext;
        this.interventionService = interventionService;
        this.actionService = actionService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try (var ctx = LoggingUtils.createContext()) {
            long studyId = context.getJobDetail().getJobDataMap().getLong("studyId");
            Integer studyGroupId = (Integer) context.getJobDetail().getJobDataMap().getOrDefault("studyGroupId", null);
            int interventionId = context.getJobDetail().getJobDataMap().getIntValue("interventionId");

            ctx.putStudy(studyId);
            ctx.putStudyGroup(studyGroupId);
            ctx.putIntervention(interventionId);
            LOGGER.debug("Execute Trigger-Job: {}", context.getTrigger());

            Trigger trigger = Optional.ofNullable(
                    interventionService.getTriggerByIds(studyId, interventionId)
            ).orElseThrow(() ->
                    new SchedulingException(String.format("Cannot find trigger: sid:%s, iid:%s", studyId, interventionId))
            );

            TriggerFactory factory = factory(trigger)
                    .orElseThrow(() -> new SchedulingException("Cannot find triggerType " + trigger.getType()));

            Intervention intervention = interventionService.getIntervention(studyId, interventionId);
            Integer milestoneId = Objects.equals(trigger.getType(), "relative-time-trigger")
                    ? intervention.getMilestoneId()
                    : null;

            MoreTriggerSDK sdk = moreSDK.scopedTriggerSDK(studyId, studyGroupId, interventionId, milestoneId);
            Parameters parameters = new Parameters(Map.of("triggerTime", context.getFireTime()));

            TriggerResult result = factory.create(sdk, trigger.getProperties()).execute(parameters);

            if (result.proceed()) {
                actionService.execute(studyId, studyGroupId, interventionId, result.getActionParameters());
            } else {
                LOGGER.debug("Skipping Action execution, trigger did not fire");
            }
        } catch (Exception e) {
            LOGGER.warn("Cannot execute Trigger-Job: {}", e.getMessage(), e);
        }

    }

    private Optional<TriggerFactory> factory(Trigger trigger) {
        try {
            return Optional.of(applicationContext.getBean(trigger.getType(), TriggerFactory.class));
        } catch (NoSuchBeanDefinitionException | BeanNotOfRequiredTypeException e) {
            return Optional.empty();
        }
    }

}
