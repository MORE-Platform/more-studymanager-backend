package io.redlink.more.studymanager.core.sdk;

import io.redlink.more.studymanager.core.io.Timeframe;
import io.redlink.more.studymanager.core.sdk.schedule.Schedule;

import java.util.Set;

public interface MoreTriggerSDK extends MorePlatformSDK {
    String addSchedule(Schedule schedule);
    void removeSchedule(String id);
    Set<Integer> participantIdsMatchingQuery(String query, Timeframe timeframe, boolean inverse);
    String addWebhook();
    void removeWebhook();
}
