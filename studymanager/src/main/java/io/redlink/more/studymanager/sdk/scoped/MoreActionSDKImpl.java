package io.redlink.more.studymanager.sdk.scoped;

import io.redlink.more.studymanager.core.component.Action;
import io.redlink.more.studymanager.core.sdk.MoreActionSDK;
import io.redlink.more.studymanager.sdk.MoreSDK;

public class MoreActionSDKImpl extends MorePlatformSDKImpl implements MoreActionSDK {

    public MoreActionSDKImpl(MoreSDK sdk, long studyId, Integer studyGroupId, Integer participantId) {
        super(sdk, studyId, studyGroupId, participantId);
    }

    @Override
    public void setAction(Action action) {

    }

    @Override
    public void sendPushNotification(String message) {

    }
}
