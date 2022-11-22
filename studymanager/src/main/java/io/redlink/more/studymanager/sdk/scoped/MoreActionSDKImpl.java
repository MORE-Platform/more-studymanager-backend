package io.redlink.more.studymanager.sdk.scoped;

import io.redlink.more.studymanager.core.component.Action;
import io.redlink.more.studymanager.core.sdk.MoreActionSDK;
import io.redlink.more.studymanager.sdk.MoreSDK;

public class MoreActionSDKImpl extends MorePlatformSDKImpl implements MoreActionSDK {

    private final int interventionId;
    private final int actionId;

    protected final int participantId;

    public MoreActionSDKImpl(MoreSDK sdk, long studyId, Integer studyGroupId, int interventionId, int actionId, int participantId) {
        super(sdk, studyId, studyGroupId);
        this.interventionId = interventionId;
        this.actionId = actionId;
        this.participantId = participantId;
    }

    @Override
    public void setAction(Action action) {

    }

    @Override
    public void sendPushNotification(String message) {

    }
}
