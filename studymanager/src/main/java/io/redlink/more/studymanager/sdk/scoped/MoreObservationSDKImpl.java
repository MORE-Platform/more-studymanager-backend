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
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;
import io.redlink.more.studymanager.core.ui.DataViewRow;
import io.redlink.more.studymanager.core.ui.ViewConfig;
import io.redlink.more.studymanager.model.data.ElasticDataPoint;
import io.redlink.more.studymanager.sdk.MoreSDK;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MoreObservationSDKImpl extends MorePlatformSDKImpl implements MoreObservationSDK {

    private final int observationId;

    public MoreObservationSDKImpl(MoreSDK sdk, long studyId, Integer studyGroupId, int observationId) {
        super(sdk, studyId, studyGroupId);
        this.observationId = observationId;
    }

    @Override
    public <T extends Serializable> void setValue(String name, T value) {
        sdk.nvpairs.setObservationValue(studyId, observationId, name, value);
    }

    @Override
    public <T extends Serializable> Optional<T> getValue(String name, Class<T> tClass) {
        return sdk.nvpairs.getObservationValue(studyId, observationId, name, tClass);
    }

    @Override
    public void removeValue(String name) {
        sdk.nvpairs.removeObservationValue(studyId, observationId, name);
    }

    @Override
    public int getObservationId() {
        return observationId;
    }

    @Override
    public void setPropertiesForParticipant(Integer participantId, ObservationProperties properties) {
        sdk.setPropertiesForParticipant(
                this.studyId,
                participantId,
                this.observationId,
                properties
        );
    }

    @Override
    public Optional<ObservationProperties> getPropertiesForParticipant(Integer participantId) {
        return sdk.getPropertiesForParticipant(
                this.studyId,
                participantId,
                this.observationId
        );
    }

    @Override
    public void removePropertiesForParticipant(Integer participantId) {
        sdk.removePropertiesForParticipant(
                this.studyId,
                participantId,
                this.observationId
        );
    }

    @Override
    public void storeDataPoint(Integer participantId, String observationType, Map data) {
        sdk.storeDatapoint(ElasticDataPoint.Type.observation, studyId, studyGroupId, participantId, observationId, observationType, Instant.now(), data);
    }

    @Override
    public List<DataViewRow> queryData(ViewConfig viewConfig, Integer participantId, TimeRange timerange) {
        return sdk.queryData(viewConfig, studyId, studyGroupId, observationId, participantId, timerange);
    }
}
