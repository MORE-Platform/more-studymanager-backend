package io.redlink.more.studymanager.sdk.scoped;

import io.redlink.more.studymanager.core.sdk.MoreActionSDK;
import io.redlink.more.studymanager.sdk.MoreSDK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;

public class MoreActionSDKImpl extends MorePlatformSDKImpl implements MoreActionSDK {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoreActionSDKImpl.class);
    private final int interventionId;
    private final int actionId;
    private final String actionType;

    protected final int participantId;

    public MoreActionSDKImpl(MoreSDK sdk, long studyId, Integer studyGroupId, int interventionId, int actionId, String actionType, int participantId) {
        super(sdk, studyId, studyGroupId);
        this.interventionId = interventionId;
        this.actionId = actionId;
        this.actionType = actionType;
        this.participantId = participantId;
    }

    @Override
    public void sendPushNotification(String title, String message) {
        if(sdk.sendPushNotification(studyId, participantId, title, message)) {
            sdk.storeActionDatapoint(studyId, studyGroupId, participantId, actionId, actionType, Instant.now(), Map.of("title", title, "message", message));
        }
    }

    @Override
    public String getIssuer() {
        return studyId + "-" + studyGroupId + '-' + interventionId + "-" + actionId + "-action";
    }
}
