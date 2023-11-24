/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.sdk.scoped;

import io.redlink.more.studymanager.core.sdk.MorePlatformSDK;
import io.redlink.more.studymanager.model.Participant;
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
    public Set<Integer> participantIds(ParticipantFilter filter) {
        Set<Participant.Status> state =
                (filter == ParticipantFilter.ACTIVE_ONLY ? Set.of(Participant.Status.ACTIVE) : null);
        return sdk.listParticipants(studyId, studyGroupId, state);
    }

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
