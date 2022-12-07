package io.redlink.more.studymanager.sdk.scoped;

import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.core.sdk.schedule.Schedule;
import io.redlink.more.studymanager.core.io.Timeframe;
import io.redlink.more.studymanager.sdk.MoreSDK;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Set;

public class MoreTriggerSDKImpl extends MorePlatformSDKImpl implements MoreTriggerSDK {

    private final int interventionId;

    public MoreTriggerSDKImpl(MoreSDK sdk, long studyId, Integer studyGroupId, int interventionId) {
        super(sdk, studyId, studyGroupId);
        this.interventionId = interventionId;
    }

    @Override
    public String addSchedule(Schedule schedule) {
        return sdk.addSchedule(getIssuer(), studyId, studyGroupId, interventionId, schedule);
    }

    @Override
    public void removeSchedule(String id) {
        sdk.removeSchedule(getIssuer(), id);
    }

    @Override
    public Set<Integer> participantIds() {
        return sdk.listActiveParticipants(studyId, studyGroupId);
    }

    public Set<Integer> participantIdsMatchingQuery(String query, Timeframe timeframe, boolean inverse) {
        return sdk.listActiveParticipantsByQuery(studyId, studyGroupId, query, timeframe, inverse);
    }

    @Override
    public String addWebhook() {
        throw new NotImplementedException();
    }

    @Override
    public void removeWebhook() {
        throw new NotImplementedException();
    }

    @Override
    public String getIssuer() {
        return studyId + "-" + studyGroupId + '-' + interventionId + "-trigger";
    }
}
