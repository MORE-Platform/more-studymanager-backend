package io.redlink.more.studymanager.sdk.scoped;

import io.redlink.more.studymanager.core.sdk.MoreActionSDK;
import io.redlink.more.studymanager.sdk.MoreSDK;
import io.redlink.more.studymanager.utils.LoggingUtils;

import java.time.Instant;
import java.util.Map;

public class MoreActionSDKImpl extends MorePlatformSDKImpl implements MoreActionSDK {
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
        try (var ctx = LoggingUtils.createContext()) {
            ctx.putStudy(studyId);
            ctx.putStudyGroup(studyGroupId);
            ctx.putParticipant(participantId);
            ctx.putIntervention(interventionId);
            ctx.putAction(actionId, actionType);

            if (sdk.sendPushNotification(studyId, participantId, title, message)) {
                sdk.storeDatapoint(studyId, studyGroupId, participantId, "action_"+actionId, actionType, Instant.now(), Map.of("title", title, "message", message));
            }
        }
    }

    @Override
    public String getIssuer() {
        return studyId + "-" + studyGroupId + '-' + interventionId + "-" + actionId + "-action";
    }
}
