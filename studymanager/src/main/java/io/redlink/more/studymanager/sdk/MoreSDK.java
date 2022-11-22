package io.redlink.more.studymanager.sdk;

import io.redlink.more.studymanager.core.sdk.MoreActionSDK;
import io.redlink.more.studymanager.core.sdk.MorePlatformSDK;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.repository.NameValuePairRepository;
import io.redlink.more.studymanager.sdk.scoped.MoreActionSDKImpl;
import io.redlink.more.studymanager.sdk.scoped.MorePlatformSDKImpl;
import io.redlink.more.studymanager.sdk.scoped.MoreTriggerSDKImpl;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Optional;

@Component
public class MoreSDK {

    public final NameValuePairRepository nvpairs;

    public MoreSDK(NameValuePairRepository nvpairs) {
        this.nvpairs = nvpairs;
    }

    <T extends Serializable> void setValue(String issuer, String name, T value) {
        nvpairs.setValue(issuer, name, value);
    }

    <T extends Serializable> Optional<T> getValue(String issuer, String name, Class<T> tClass) {
        return nvpairs.getValue(issuer, name, tClass);
    }

    void removeValue(String issuer, String name) {
        nvpairs.removeValue(issuer, name);
    }

    public MoreActionSDK scopedActionSDK(Long studyId, int studyGroupId, int participantId) {
        return new MoreActionSDKImpl(this, studyId, studyGroupId, participantId);
    }

    public MorePlatformSDK scopedPlatformSDK(Long studyId, int studyGroupId) {
        return new MorePlatformSDKImpl(this, studyId, studyGroupId, null);
    }

    public MoreTriggerSDK scopedTriggerSDK(Long studyId, int studyGroupId) {
        return new MoreTriggerSDKImpl(this, studyId, studyGroupId);
    }
}
