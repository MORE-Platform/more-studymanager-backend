package io.redlink.more.studymanager.sdk;

import io.redlink.more.studymanager.core.sdk.MoreActionSDK;
import io.redlink.more.studymanager.core.sdk.MorePlatformSDK;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.core.sdk.schedule.Schedule;
import io.redlink.more.studymanager.repository.NameValuePairRepository;
import io.redlink.more.studymanager.sdk.scoped.MoreActionSDKImpl;
import io.redlink.more.studymanager.sdk.scoped.MoreObservationSDKImpl;
import io.redlink.more.studymanager.sdk.scoped.MoreTriggerSDKImpl;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Optional;

@Component
public class MoreSDK {

    private final NameValuePairRepository nvpairs;

    public MoreSDK(NameValuePairRepository nvpairs) {
        this.nvpairs = nvpairs;
    }

    public <T extends Serializable> void setValue(String issuer, String name, T value) {
        nvpairs.setValue(issuer, name, value);
    }

    public <T extends Serializable> Optional<T> getValue(String issuer, String name, Class<T> tClass) {
        return nvpairs.getValue(issuer, name, tClass);
    }

    public void removeValue(String issuer, String name) {
        nvpairs.removeValue(issuer, name);
    }

    public MoreActionSDK scopedActionSDK(Long studyId, int studyGroupId, int interventionId, int actionId, int participantId) {
        return new MoreActionSDKImpl(this, studyId, studyGroupId, interventionId, actionId, participantId);
    }

    public MorePlatformSDK scopedPlatformSDK(Long studyId, int studyGroupId, int observationId) {
        return new MoreObservationSDKImpl(this, studyId, studyGroupId, observationId);
    }

    public MoreTriggerSDK scopedTriggerSDK(Long studyId, Integer studyGroupId, int interventionId) {
        return new MoreTriggerSDKImpl(this, studyId, studyGroupId, interventionId);
    }

    public String addSchedule(String issuer, long studyId, Integer studyGroupId, int interventionId, Schedule schedule) {
        return null;
    }

    public void removeSchedule(String issuer, String id) {

    }
}
