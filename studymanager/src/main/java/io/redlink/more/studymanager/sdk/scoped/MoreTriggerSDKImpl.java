package io.redlink.more.studymanager.sdk.scoped;

import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.core.sdk.schedule.Schedule;
import io.redlink.more.studymanager.sdk.MoreSDK;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Optional;
public class MoreTriggerSDKImpl extends MorePlatformSDKImpl implements MoreTriggerSDK {

    private final int interventionId;

    public MoreTriggerSDKImpl(MoreSDK sdk, long studyId, Integer studyGroupId, int interventionId) {
        super(sdk, studyId, studyGroupId);
        this.interventionId = interventionId;
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
        throw new NotImplementedException();
    }

    @Override
    public void removeWebhook() {
        throw new NotImplementedException();
    }
}
