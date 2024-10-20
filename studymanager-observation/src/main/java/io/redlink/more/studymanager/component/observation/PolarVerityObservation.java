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
import io.redlink.more.studymanager.core.ui.DataView;
import io.redlink.more.studymanager.core.ui.DataViewData;
import io.redlink.more.studymanager.core.ui.DataViewInfo;
import io.redlink.more.studymanager.core.ui.ViewConfig;
import java.util.List;

public class PolarVerityObservation<C extends ObservationProperties> extends Observation<C> {
    public PolarVerityObservation(MoreObservationSDK sdk, C properties) throws ConfigurationValidationException {
        super(sdk, properties);
    }

    private enum DataViewInfoType implements DataViewInfo {
        heart_rate("avgHeartRate",
                DataView.ChartType.LINE,
                new ViewConfig(
                        List.of(),
                        ViewConfig.Aggregation.PARTICIPANT,
                        ViewConfig.Aggregation.TIME,
                        new ViewConfig.Operation(ViewConfig.Operator.AVG, "hr")
                )
        );

        private final String label;
        private final String title;
        private final String description;
        private final DataView.ChartType chartType;

        private final ViewConfig viewConfig;

        DataViewInfoType(String i18nKey, DataView.ChartType chartType, ViewConfig viewConfig) {
            this(
                    "monitoring.charts.polarVerity.%s.label".formatted(i18nKey),
                    "monitoring.charts.polarVerity.%s.title".formatted(i18nKey),
                    "monitoring.charts.polarVerity.%s.description".formatted(i18nKey),
                    chartType,
                    viewConfig
            );
        }

        DataViewInfoType(String label, String title, String description, DataView.ChartType chartType, ViewConfig viewConfig) {
            this.label = label;
            this.title = title;
            this.description = description;
            this.chartType = chartType;
            this.viewConfig = viewConfig;
        }

        @Override
        public String label() {
            return this.label;
        }

        @Override
        public String title() {
            return this.title;
        }

        @Override
        public String description() {
            return this.description;
        }

        public DataView.ChartType chartType() {
            return chartType;
        }

        public ViewConfig getViewConfig() {
            return viewConfig;
        }
    }

    public DataViewInfo[] listViews() {
        return DataViewInfoType.values();
    }

    @Override
    public DataView getView(String viewName, Integer studyGroupId, Integer participantId, TimeRange timerange) {
        final DataViewInfoType dataView = DataViewInfoType.valueOf(viewName);
        final DataViewData dataViewData = sdk.queryData(dataView.getViewConfig(), studyGroupId, participantId, timerange);

        return new DataView(
                dataView,
                dataView.chartType(),
                dataViewData
        );

    }
}
