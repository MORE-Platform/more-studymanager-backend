package io.redlink.more.studymanager.sdk.scoped;

import io.redlink.more.studymanager.sdk.MoreSDK;

public class MoreObservationSDKImpl extends MorePlatformSDKImpl {

    private final int observationId;

    public MoreObservationSDKImpl(MoreSDK sdk, long studyId, Integer studyGroupId, int observationId) {
        super(sdk, studyId, studyGroupId);
        this.observationId = observationId;
    }

    @Override
    public String getIssuer() {
        return studyId + "-" + studyGroupId + '-' + observationId + "-observation";
    }
}
