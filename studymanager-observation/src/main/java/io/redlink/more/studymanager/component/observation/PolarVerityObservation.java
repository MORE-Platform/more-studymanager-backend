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

    private enum DataViewInfoType implements DataViewInfo {
        heart_rate("Heart rate", "Heart rate per participant over time.");

        private final String title;
        private final String description;

        DataViewInfoType(String title, String description) {
            this.title = title;
            this.description = description;
        }

        @Override
        public String title() {
            return this.title;
        }

        @Override
        public String description() {
            return this.description;
        }
    }

    public Set<DataViewInfo> listViews() {
        return Stream.of(DataViewInfoType.values())
                .collect(Collectors.toSet());
    }

    @Override
    public DataView getView(String viewName, Integer studyGroupId, Integer participantId, TimeRange timerange) {
        return switch (DataViewInfoType.valueOf(viewName)) {
            case heart_rate -> createHeartRateView(studyGroupId, participantId, timerange);
        };
    }

    private DataView createHeartRateView(Integer studyGroupId, Integer participantId, TimeRange timerange) {
        var viewConfig = new ViewConfig(
                List.of(),
                ViewConfig.Aggregation.PARTICIPANT,
                ViewConfig.Aggregation.TIME,
                new ViewConfig.Operation(ViewConfig.Operator.AVG, "hr")
        );

        DataViewData dataViewData = sdk.queryData(viewConfig, studyGroupId, participantId, timerange);
        return new DataView(
                DataViewInfoType.heart_rate,
                DataView.ChartType.LINE,
                dataViewData
        );
    }
}
