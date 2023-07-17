package io.redlink.more.studymanager.component.trigger;

import io.redlink.more.studymanager.core.component.Trigger;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.io.ActionParameter;
import io.redlink.more.studymanager.core.io.Parameters;
import io.redlink.more.studymanager.core.io.TriggerResult;
import io.redlink.more.studymanager.core.properties.TriggerProperties;
import io.redlink.more.studymanager.core.sdk.MorePlatformSDK;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.core.sdk.schedule.CronSchedule;

import java.util.Set;
import java.util.stream.Collectors;

public class ScheduledTrigger extends Trigger<TriggerProperties> {

    protected ScheduledTrigger(MoreTriggerSDK sdk, TriggerProperties properties) throws ConfigurationValidationException {
        super(sdk, properties);
    }

    @Override
    public void activate() {
        String schedule = sdk.addSchedule(new CronSchedule(properties.getString("cronSchedule")));
        sdk.setValue("scheduleId", schedule);
    }

    @Override
    public void deactivate() {
        sdk.getValue("scheduleId", String.class).ifPresent(sdk::removeSchedule);
    }

    @Override
    public TriggerResult execute(Parameters parameters) {
        Set<Integer> participants = sdk.participantIds(MorePlatformSDK.ParticipantFilter.ACTIVE_ONLY);
        if(participants.isEmpty()) {
           return TriggerResult.NOOP;
        } else {
            return TriggerResult.withParams(
                    participants.stream().map(p -> new ActionParameter(sdk.getStudyId(), p)).collect(Collectors.toSet())
            );
        }
    }
}
