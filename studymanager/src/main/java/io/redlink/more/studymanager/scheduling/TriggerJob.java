package io.redlink.more.studymanager.scheduling;

import io.redlink.more.studymanager.core.exception.SchedulingException;
import io.redlink.more.studymanager.core.factory.TriggerFactory;
import io.redlink.more.studymanager.core.io.Parameters;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.model.Intervention;
import io.redlink.more.studymanager.model.Trigger;
import io.redlink.more.studymanager.repository.InterventionRepository;
import io.redlink.more.studymanager.sdk.MoreSDK;
import io.redlink.more.studymanager.service.InterventionService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class TriggerJob implements Job {

    private static Logger LOGGER = LoggerFactory.getLogger(TriggerJob.class);

    @Autowired
    private MoreSDK moreSDK;

    @Autowired
    private Map<String, TriggerFactory> triggertFactories;

    @Autowired
    private InterventionService interventionService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        long studyId = context.getJobDetail().getJobDataMap().getLong("studyId");
        Integer studyGroupId = (Integer) context.getJobDetail().getJobDataMap().getOrDefault("studyGroupIdl", null);
        int interventionId = context.getJobDetail().getJobDataMap().getIntValue("interventionId");

        Trigger trigger = Optional.ofNullable(
                interventionService.getTriggerByIds(studyId, interventionId)
        ).orElseThrow(() ->
                new SchedulingException(String.format("Cannot find trigger: sid:%s, iid:%s", studyId, interventionId))
        );

        TriggerFactory factory = Optional.ofNullable(
                triggertFactories.get(trigger.getType())
        ).orElseThrow(() -> new SchedulingException("Cannot find triggerType " + trigger.getType()));

        MoreTriggerSDK sdk = moreSDK.scopedTriggerSDK(studyId, studyGroupId, interventionId);
        Parameters parameters = new Parameters(Map.of("triggerTime", context.getFireTime()));

        factory.create(sdk, trigger.getProperties()).execute(parameters);
    }
}
