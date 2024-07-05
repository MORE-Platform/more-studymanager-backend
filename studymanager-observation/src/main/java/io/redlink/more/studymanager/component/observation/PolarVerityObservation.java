/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.component.observation;

import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.io.TimeRange;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;
import io.redlink.more.studymanager.core.ui.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PolarVerityObservation<C extends ObservationProperties> extends Observation<C> {
    public PolarVerityObservation(MoreObservationSDK sdk, C properties) throws ConfigurationValidationException {
        super(sdk, properties);
    }

    private enum DataViewInfoType {
        HEART_RATE("heartRate", "Heart rate", "Heart rate per participant over time.");

        private final DataViewInfo dataViewInfo;

        DataViewInfoType(String key, String displayName, String description) {
            this.dataViewInfo = new DataViewInfo(key, displayName, description);
        }

        public DataViewInfo getDataViewInfo() {
            return dataViewInfo;
        }
    }

    public Set<DataViewInfo> listViews() {
        return Stream.of(DataViewInfoType.values())
                .map(DataViewInfoType::getDataViewInfo)
                .collect(Collectors.toSet());
    }

    @Override
    public DataView getView(String viewId, Integer studyGroupId, Integer participantId, TimeRange timerange) {
        return switch (viewId) {
            case "hr" -> createHeartRateView(studyGroupId, participantId, timerange);
            default -> null;
        };
    }

    private DataView createHeartRateView(Integer studyGroupId, Integer participantId, TimeRange timerange) {
        var viewConfig = new ViewConfig(
                List.of(),
                ViewConfig.Aggregation.PARTICIPANT,
                ViewConfig.Aggregation.TIME,
                new ViewConfig.Operation(ViewConfig.Operator.AVG, "hr")
        );

        List<DataViewRow> rows = sdk.queryData(viewConfig, participantId, timerange);
        return new DataView(
                DataViewInfoType.HEART_RATE.getDataViewInfo(),
                DataView.ChartType.LINE,
                List.of(),
                rows
        );
    }
}
