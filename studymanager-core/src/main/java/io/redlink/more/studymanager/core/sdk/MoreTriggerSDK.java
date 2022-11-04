package io.redlink.more.studymanager.core.sdk;

import io.redlink.more.studymanager.core.component.Trigger;
import io.redlink.more.studymanager.core.sdk.schedule.Schedule;

import java.util.Optional;

public interface MoreTriggerSDK extends MorePlatformSDK {
    //TODO could be cooler
    void setTrigger(Trigger trigger);
    Optional<String> addSchedule(Schedule schedule);
    void removeSchedule(String id);

    Optional<String> addWebhook();
    void removeWebhook();
}
