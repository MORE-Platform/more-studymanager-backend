package io.redlink.more.studymanager.sdk.scoped;

import io.redlink.more.studymanager.core.sdk.MorePlatformSDK;
import io.redlink.more.studymanager.sdk.MoreSDK;

import java.util.Optional;

public class MorePlatformSDKImpl implements MorePlatformSDK {

    protected final long studyId;

    protected final Integer studyGroupId;
    protected final MoreSDK sdk;

    MorePlatformSDKImpl(MoreSDK sdk, long studyId, Integer studyGroupId) {
        this.studyId = studyId;
        this.studyGroupId = studyGroupId;
        this.sdk = sdk;
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
