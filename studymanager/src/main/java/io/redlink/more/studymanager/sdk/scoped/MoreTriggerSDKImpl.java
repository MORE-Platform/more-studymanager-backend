package io.redlink.more.studymanager.sdk.scoped;

import io.redlink.more.studymanager.core.component.Trigger;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.core.sdk.schedule.Schedule;
import io.redlink.more.studymanager.sdk.MoreSDK;

import java.util.Optional;
public class MoreTriggerSDKImpl extends MorePlatformSDKImpl implements MoreTriggerSDK {

    public MoreTriggerSDKImpl(MoreSDK sdk, long studyId, Integer studyGroupId) {
        super(sdk, studyId, studyGroupId, null);
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
