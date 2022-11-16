package io.redlink.more.studymanager.sdk;

import io.redlink.more.studymanager.core.component.Trigger;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.core.sdk.schedule.Schedule;
import org.springframework.stereotype.Component;

import java.util.Optional;
@Component("MoreTriggerSDKImpl")
public class MoreTriggerSDKImpl implements MoreTriggerSDK {
    @Override
    public <T> void setValue(String name, T value) {

    }

    @Override
    public <T> Optional<T> getValue(String name, Class<T> tClass) {
        return Optional.empty();
    }

    @Override
    public void removeValue(String name) {

    }

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
