package io.redlink.more.studymanager.sdk;

import io.redlink.more.studymanager.core.component.Action;
import io.redlink.more.studymanager.core.sdk.MoreActionSDK;
import io.redlink.more.studymanager.core.sdk.MorePlatformSDK;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("MoreActionSDKImpl")
public class MoreActionSDKImpl extends MorePlatformSDKImpl implements MoreActionSDK {

    @Override
    public void setAction(Action action) {

    }

    @Override
    public void sendPushNotification(String message) {

    }
}
