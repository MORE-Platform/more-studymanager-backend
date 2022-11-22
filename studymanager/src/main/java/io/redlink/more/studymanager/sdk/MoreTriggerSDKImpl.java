package io.redlink.more.studymanager.sdk;

import io.redlink.more.studymanager.core.component.Trigger;
import io.redlink.more.studymanager.core.sdk.MorePlatformSDK;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.core.sdk.schedule.Schedule;
import org.springframework.stereotype.Component;

import java.util.Optional;
@Component("MoreTriggerSDKImpl")
public class MoreTriggerSDKImpl extends MorePlatformSDKImpl implements MoreTriggerSDK {

    @Override
    public void setTrigger(Trigger trigger) {

    }

    @Override
    public Optional<String> addSchedule(Schedule schedule) {
        return Optional.empty();
    }

    @Override
    public void removeSchedule(String id) {

    }

    @Override
    public Optional<String> addWebhook() {
        return Optional.empty();
    }

    @Override
    public void removeWebhook() {

    }
}
