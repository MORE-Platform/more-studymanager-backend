package io.redlink.more.studymanager.component.trigger.datacheck;

import io.redlink.more.studymanager.core.component.Trigger;
import io.redlink.more.studymanager.core.io.*;
import io.redlink.more.studymanager.core.sdk.MorePlatformSDK;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.core.sdk.schedule.CronSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

public class ScheduledDatacheckTrigger extends Trigger<ScheduledDatacheckTriggerProperties> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledDatacheckTrigger.class);

    private static final String SCHEDULE_ID = "scheduleId";

    protected ScheduledDatacheckTrigger(MoreTriggerSDK sdk, ScheduledDatacheckTriggerProperties properties) {
        super(sdk, properties);
    }

    @Override
    public void activate() {
        sdk.participantIds(MorePlatformSDK.ParticipantFilter.ALL).forEach(id -> setParticipantActive(id, false));

        properties.getCronSchedule()
                .map(CronSchedule::new)
                .map(sdk::addSchedule)
                .ifPresent(id -> {
                    sdk.setValue(SCHEDULE_ID, id);
                    LOGGER.info("Activated");
                });
    }

    @Override
    public void deactivate() {
        sdk.getValue(SCHEDULE_ID, String.class).ifPresent(sdk::removeSchedule);
    }

    @Override
    public TriggerResult execute(Parameters parameters) {
        LOGGER.info("Execute trigger with params: {}",
                parameters.keySet().stream()
                        .map(key -> key + "=" + parameters.get(key))
                        .collect(Collectors.joining(", ", "{", "}")));

        TimeRange timeframe = properties.getWindow()
                .map(window -> new RelativeTimeFrame(window))
                .orElse(null);

        Set<Integer> notMatchingParticipantIds = sdk.participantIds(MorePlatformSDK.ParticipantFilter.ACTIVE_ONLY);

        TriggerResult result = properties.getElasticQueryString().map(query ->
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
