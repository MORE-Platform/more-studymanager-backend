package io.redlink.more.studymanager.scheduling;

import io.redlink.more.studymanager.action.ActionService;
import io.redlink.more.studymanager.core.exception.SchedulingException;
import io.redlink.more.studymanager.core.factory.TriggerFactory;
import io.redlink.more.studymanager.core.io.Parameters;
import io.redlink.more.studymanager.core.io.TriggerResult;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.model.Trigger;
import io.redlink.more.studymanager.sdk.MoreSDK;
import io.redlink.more.studymanager.service.InterventionService;
import io.redlink.more.studymanager.utils.LoggingUtils;
import java.util.Map;
import java.util.Optional;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

public class TriggerJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(TriggerJob.class);

    @Autowired
    private MoreSDK moreSDK;

    @Autowired
    private Map<String, TriggerFactory> triggerFactories;

    @Autowired
    private InterventionService interventionService;

    @Autowired
    private ActionService actionService;

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
                    interventionService.getTriggerByIds(studyId, interventionId, null)
            ).orElseThrow(() ->
                    new SchedulingException(String.format("Cannot find trigger: sid:%s, iid:%s", studyId, interventionId))
            );

            TriggerFactory factory = Optional.ofNullable(
                    triggerFactories.get(trigger.getType())
            ).orElseThrow(() -> new SchedulingException("Cannot find triggerType " + trigger.getType()));

            MoreTriggerSDK sdk = moreSDK.scopedTriggerSDK(studyId, studyGroupId, interventionId);
            Parameters parameters = new Parameters(Map.of("triggerTime", context.getFireTime()));

            TriggerResult result = factory.create(sdk, trigger.getProperties()).execute(parameters);

            if(result.proceed()) {
                actionService.execute(studyId, studyGroupId, interventionId, result.getActionParameters());
            } else {
                LOGGER.debug("Skipping Action execution, trigger did not fire");
            }
        } catch (Exception e) {
            LOGGER.warn("Cannot execute Trigger-Job: {}", e.getMessage(), e);
        }

    }
}
