package io.redlink.more.studymanager.component.trigger.api;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.redlink.more.studymanager.core.component.Trigger;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.io.TriggerResult;
import io.redlink.more.studymanager.core.properties.TriggerProperties;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.core.sdk.schedule.CronSchedule;
import io.redlink.more.studymanager.core.io.ActionParameter;
import io.redlink.more.studymanager.core.io.Parameters;


public class ApiTrigger extends Trigger<TriggerProperties> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiTrigger.class);
    public static final String PENDING_PARTICIPANTS_KEY = "pendingParticipants";

    protected ApiTrigger(MoreTriggerSDK sdk, TriggerProperties properties) throws ConfigurationValidationException {
        super(sdk, properties);
    }

    @Override
    public void activate() {
        // Poll every 5 seconds for pending trigger requests
        String schedule = sdk.addSchedule(new CronSchedule("*/5 * * * * ?"));
        sdk.setValue("scheduleId", schedule);
    }

    @Override
    public void deactivate() {
        sdk.getValue("scheduleId", String.class).ifPresent(sdk::removeSchedule);
    }

    @Override
    @SuppressWarnings("unchecked")
    public TriggerResult execute(Parameters parameters) {
        // Read pending participants from storage
        Set<Integer> pending = sdk.getValue(PENDING_PARTICIPANTS_KEY, HashSet.class)
                .orElse(new HashSet<>());

        if (pending.isEmpty()) {
            return TriggerResult.NOOP;
        }

        LOGGER.info("Execute API trigger on study {} - triggering for {} participant(s): {}",
                sdk.getStudyId(), pending.size(), pending);

        // Build action parameters for all pending participants
        Set<ActionParameter> actionParams = pending.stream()
                .map(pid -> new ActionParameter(sdk.getStudyId(), pid))
                .collect(Collectors.toSet());

        // Clear pending list after processing
        sdk.removeValue(PENDING_PARTICIPANTS_KEY);

        return TriggerResult.withParams(actionParams);
    }
}
