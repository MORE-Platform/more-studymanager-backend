package io.redlink.more.studymanager.sdk;

import io.redlink.more.studymanager.core.sdk.MorePlatformSDK;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MorePlatformSDKImpl implements MorePlatformSDK {
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
