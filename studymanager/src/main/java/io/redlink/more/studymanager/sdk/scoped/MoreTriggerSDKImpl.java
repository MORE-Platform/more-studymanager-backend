/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.sdk.scoped;

import io.redlink.more.studymanager.core.io.TimeRange;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.core.sdk.schedule.Schedule;
import io.redlink.more.studymanager.sdk.MoreSDK;
import org.apache.commons.lang3.NotImplementedException;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

public class MoreTriggerSDKImpl extends MorePlatformSDKImpl implements MoreTriggerSDK {

    private final int interventionId;

    public MoreTriggerSDKImpl(MoreSDK sdk, long studyId, Integer studyGroupId, int interventionId) {
        super(sdk, studyId, studyGroupId);
        this.interventionId = interventionId;
    }

    @Override
    public <T extends Serializable> void setValue(String name, T value) {
        sdk.nvpairs.setTriggerValue(studyId, interventionId, name, value);
    }

    @Override
    public <T extends Serializable> Optional<T> getValue(String name, Class<T> tClass) {
        return sdk.nvpairs.getTriggerValue(studyId, interventionId, name, tClass);
    }

    @Override
    public void removeValue(String name) {
        sdk.nvpairs.removeTriggerValue(studyId, interventionId, name);
    }

    @Override
    public String addSchedule(Schedule schedule) {
        return sdk.addSchedule(getIssuer(), studyId, studyGroupId, interventionId, schedule);
    }

    @Override
    public void removeSchedule(String id) {
        sdk.removeSchedule(getIssuer(), id);
    }

    public Set<Integer> participantIdsMatchingQuery(String query, TimeRange timerange) {
        return sdk.listActiveParticipantsByQuery(studyId, studyGroupId, query, timerange);
    }

    @Override
    public String addWebhook() {
        throw new NotImplementedException();
    }

    @Override
    public void removeWebhook() {
        throw new NotImplementedException();
    }

    private String getIssuer() {
        return studyId + "-" + studyGroupId + '-' + interventionId + "-trigger";
    }
}
