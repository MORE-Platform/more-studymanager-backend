package io.redlink.more.studymanager.core.sdk;

import io.redlink.more.studymanager.core.component.Trigger;
import io.redlink.more.studymanager.core.sdk.schedule.Schedule;

import java.util.Optional;
import java.util.Set;

public interface MoreTriggerSDK extends MorePlatformSDK {
    String addSchedule(Schedule schedule);
    void removeSchedule(String id);
    Set<Integer> participantIds();
    Set<Integer> participantIdsMatchingQuery(String query);
    String addWebhook();
    void removeWebhook();
}
