package io.redlink.more.studymanager.sdk;

import io.redlink.more.studymanager.core.component.Action;
import io.redlink.more.studymanager.core.sdk.MoreActionSDK;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("MoreActionSDKImpl")
public class MoreActionSDKImpl implements MoreActionSDK {
    @Override
    public void setAction(Action action) {

    }

    @Override
    public void sendPushNotification(String message) {

    }

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
}
