package io.redlink.more.studymanager.core.sdk;

import io.redlink.more.studymanager.core.component.Trigger;
import io.redlink.more.studymanager.core.sdk.schedule.Schedule;

import java.util.Optional;

public interface MoreTriggerSDK extends MorePlatformSDK {
    String addSchedule(Schedule schedule);
    void removeSchedule(String id);

    String addWebhook();
    void removeWebhook();
}
