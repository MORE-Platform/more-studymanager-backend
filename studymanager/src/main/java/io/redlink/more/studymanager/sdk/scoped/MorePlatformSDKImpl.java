package io.redlink.more.studymanager.sdk.scoped;

import io.redlink.more.studymanager.core.sdk.MorePlatformSDK;
import io.redlink.more.studymanager.sdk.MoreSDK;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

public abstract class MorePlatformSDKImpl implements MorePlatformSDK {

    protected final long studyId;
    protected final Integer studyGroupId;
    protected final MoreSDK sdk;

    MorePlatformSDKImpl(MoreSDK sdk, long studyId, Integer studyGroupId) {
        this.studyId = studyId;
        this.studyGroupId = studyGroupId;
        this.sdk = sdk;
    }

    @Override
    public long getStudyId() {
        return studyId;
    }

    @Override
    public Integer getStudyGroupId() {
        return studyGroupId;
    }

    @Override
    public Set<Integer> participantIds(){ return sdk.listParticipants(studyId, studyGroupId, null); }

    @Override
    public <T extends Serializable> void setValue(String name, T value) {
        sdk.setValue(getIssuer(), name, value);
    }

    @Override
    public <T extends Serializable> Optional<T> getValue(String name, Class<T> tClass) {
        return sdk.getValue(getIssuer(), name, tClass);
    }

    @Override
    public void removeValue(String name) {
        sdk.removeValue(getIssuer(), name);
    }

    public abstract String getIssuer();
}
