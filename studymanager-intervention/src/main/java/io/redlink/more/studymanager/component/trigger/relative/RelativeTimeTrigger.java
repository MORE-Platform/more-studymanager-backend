/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.component.trigger.relative;

import io.redlink.more.studymanager.core.component.Trigger;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.io.ActionParameter;
import io.redlink.more.studymanager.core.io.Parameters;
import io.redlink.more.studymanager.core.io.SimpleParticipant;
import io.redlink.more.studymanager.core.io.TriggerResult;
import io.redlink.more.studymanager.core.properties.TriggerProperties;
import io.redlink.more.studymanager.core.sdk.MorePlatformSDK;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.core.sdk.schedule.CronSchedule;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RelativeTimeTrigger extends Trigger<TriggerProperties> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RelativeTimeTrigger.class);

    // TODO set to study home as soon the feature is there
    private static final ZoneId HOME = ZoneId.of("Europe/Vienna");

    protected RelativeTimeTrigger(MoreTriggerSDK sdk, TriggerProperties properties) throws ConfigurationValidationException {
        super(sdk, properties);
    }
    @Override
    public void activate() {
        String schedule = sdk.addSchedule(new CronSchedule("1 0 * * * ?"));
        sdk.setValue("scheduleId", schedule);
    }

    @Override
    public void deactivate() {
        sdk.getValue("scheduleId", String.class).ifPresent(sdk::removeSchedule);
    }

    @Override
    public TriggerResult execute(Parameters parameters) {
        return TriggerResult.withParams(
                sdk.participants(MorePlatformSDK.ParticipantFilter.ACTIVE_ONLY).stream()
                        .filter(p -> matchesDayAndHour(p, Instant.now()))
                        .map(p -> new ActionParameter(sdk.getStudyId(), p.getId()))
                        .collect(Collectors.toSet())
        );
    }

    protected boolean matchesDayAndHour(SimpleParticipant participant, Instant now) {
        long day = ChronoUnit.DAYS.between(
                LocalDateTime.of(participant.getStart().atZone(HOME).toLocalDate(), LocalTime.MIDNIGHT),
                LocalDateTime.of(now.atZone(HOME).toLocalDate(), LocalTime.MIDNIGHT)
        ) + 1;
        int hour = now.atZone(HOME).getHour();
        return properties.getInt("hour") == hour && properties.getLong("day") == day;
    }
}
