/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.sdk.scoped;

import io.redlink.more.studymanager.core.sdk.MoreActionSDK;
import io.redlink.more.studymanager.model.data.ElasticDataPoint;
import io.redlink.more.studymanager.sdk.MoreSDK;
import io.redlink.more.studymanager.utils.LoggingUtils;
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
        try (var ctx = LoggingUtils.createContext()) {
            ctx.putStudy(studyId);
            ctx.putStudyGroup(studyGroupId);
            ctx.putParticipant(participantId);
            ctx.putIntervention(interventionId);
            ctx.putAction(actionId, actionType);

            if (sdk.sendPushNotification(studyId, participantId, title, message, null)) {
                sdk.storeDatapoint(ElasticDataPoint.Type.action, studyId, studyGroupId, participantId, actionId, actionType, Instant.now(), Map.of("title", title, "message", message));
            }
        }
    }

    @Override
    public void triggerObservation(String title, String message, String factoryId, int observationId) {
        try (var ctx = LoggingUtils.createContext()) {
            ctx.putStudy(studyId);
            ctx.putStudyGroup(studyGroupId);
            ctx.putParticipant(participantId);
            ctx.putIntervention(interventionId);
            ctx.putAction(actionId, actionType);

            //TODO still android specific
            String deepLink = String.format("app://io.redlink.more.app.android/%s?observationId=%s", factoryId, observationId);

            LOGGER.info("Trigger observation for participant {} with deepLink <{}>", participantId, deepLink);

            if (sdk.sendPushNotification(studyId, participantId, title, message, Map.of("deepLink", deepLink))) {
                sdk.storeDatapoint(ElasticDataPoint.Type.action, studyId, studyGroupId, participantId, actionId, actionType, Instant.now(), Map.of("title", title, "message", message));
            }
        }
    }

    @Override
    public String getIssuer() {
        return studyId + "-" + studyGroupId + '-' + interventionId + "-" + actionId + "-action";
    }
}
