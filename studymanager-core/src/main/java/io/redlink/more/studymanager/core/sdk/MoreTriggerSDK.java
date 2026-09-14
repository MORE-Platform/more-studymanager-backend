/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.studymanager.core.sdk;

import io.redlink.more.studymanager.core.io.TimeRange;
import io.redlink.more.studymanager.core.sdk.schedule.Schedule;

import java.util.Set;

public interface MoreTriggerSDK extends MorePlatformSDK {
    String addSchedule(Schedule schedule);
    void removeSchedule(String id);
    Set<Integer> participantIdsMatchingQuery(String query, TimeRange timeRange);
    String addWebhook();
    void removeWebhook();
}
