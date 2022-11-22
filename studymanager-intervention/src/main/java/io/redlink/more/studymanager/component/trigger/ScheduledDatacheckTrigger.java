package io.redlink.more.studymanager.component.trigger;

import io.redlink.more.studymanager.core.component.Trigger;
import io.redlink.more.studymanager.core.io.Parameters;
import io.redlink.more.studymanager.core.io.TriggerResult;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.core.sdk.schedule.CronSchedule;

public class ScheduledDatacheckTrigger extends Trigger<ScheduledDatacheckTriggerProperties> {

    private static final String SCHEDULE_ID = "scheduleId";

    protected ScheduledDatacheckTrigger(MoreTriggerSDK sdk, ScheduledDatacheckTriggerProperties properties) {
        super(sdk, properties);
    }

    @Override
    public void activate() {
        properties.getCronSchedule()
                .map(CronSchedule::new)
                .map(sdk::addSchedule)
                .ifPresent(id -> sdk.setValue(SCHEDULE_ID, id));
    }

    @Override
    public void deactivate() {
        sdk.getValue(SCHEDULE_ID, String.class).ifPresent(sdk::removeSchedule);
    }

    @Override
    public TriggerResult execute(Parameters parameters) {
        // get all participants where query maps
        // TriggerResult.withParams(Set.of(new ActionParameter("p1", null)));
        return TriggerResult.PROCEED_ALL;
    }
}
