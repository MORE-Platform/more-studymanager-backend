package io.redlink.more.studymanager.component.trigger;

import io.redlink.more.studymanager.core.component.Trigger;
import io.redlink.more.studymanager.core.io.ActionParameter;
import io.redlink.more.studymanager.core.io.Parameters;
import io.redlink.more.studymanager.core.io.Timeframe;
import io.redlink.more.studymanager.core.io.TriggerResult;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.core.sdk.schedule.CronSchedule;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ScheduledDatacheckTrigger extends Trigger<ScheduledDatacheckTriggerProperties> {

    private static final String SCHEDULE_ID = "scheduleId";

    protected ScheduledDatacheckTrigger(MoreTriggerSDK sdk, ScheduledDatacheckTriggerProperties properties) {
        super(sdk, properties);
    }

    @Override
    public void activate() {
        sdk.participantIds().forEach(id -> setParticipantActive(id, false));

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
        Timeframe timeframe = properties.getWindow()
                .map(window -> new Timeframe(Instant.now().minusMillis(window), Instant.now()))
                .orElse(null);

        Set<Integer> notMatchingParticipantIds = sdk.participantIds();

        TriggerResult result = properties.getQuery().map(query ->
                TriggerResult.withParams(
                        sdk.participantIdsMatchingQuery(query, timeframe).stream()
                                .peek(notMatchingParticipantIds::remove)
                                .filter(id -> !isParticipantActive(id))
                                .peek(id -> setParticipantActive(id, true))
                                .map(id -> new ActionParameter(sdk.getStudyId(), id))
                                .collect(Collectors.toSet())
                )
        ).orElse(TriggerResult.NOOP);

        notMatchingParticipantIds.forEach(id -> setParticipantActive(id, false));

        return result;
    }

    public boolean isParticipantActive(int participantId) {
        return sdk.getValue(participantId + "_active", Boolean.class).orElse(false);
    }

    public void setParticipantActive(int participantId, Boolean isActive) {
        sdk.setValue(participantId + "_active", isActive);
    }

}
