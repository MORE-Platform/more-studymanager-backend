/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.core.sdk;

import io.redlink.more.studymanager.core.io.TimeRange;
import io.redlink.more.studymanager.core.sdk.schedule.Schedule;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

public interface MoreTriggerSDK extends MorePlatformSDK {
    String addSchedule(Schedule schedule);

    void removeSchedule(String id);

    Set<Integer> participantIdsMatchingQuery(String query, TimeRange timeRange);

    /**
     * The date-time at which the given participant reached this trigger's configured milestone,
     * or empty if the trigger isn't milestone-anchored or the participant hasn't reached it yet.
     */
    Optional<Instant> getMilestoneDateTime(Integer participantId);

    String addWebhook();

    void removeWebhook();
}
