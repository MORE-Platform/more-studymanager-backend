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
