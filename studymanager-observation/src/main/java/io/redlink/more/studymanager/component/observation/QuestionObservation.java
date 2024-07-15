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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuestionObservation<C extends ObservationProperties> extends Observation<C> {

    public QuestionObservation(MoreObservationSDK sdk, C properties) throws ConfigurationValidationException {
        super(sdk, properties);
    }

    private enum DataViewInfoType implements DataViewInfo {
        response_distribution(
                "responseDistribution",
                DataView.ChartType.PIE,
                new ViewConfig(
                        List.of(),
                        null,
                        ViewConfig.Aggregation.TERM_FIELD,
                        new ViewConfig.Operation(ViewConfig.Operator.COUNT, "answer")
                )
        ),
        answers_by_group("responseDistributionStudyGroup",
                DataView.ChartType.BAR,
                new ViewConfig(
                        List.of(),
                        ViewConfig.Aggregation.TERM_FIELD,
                        ViewConfig.Aggregation.STUDY_GROUP,
                        new ViewConfig.Operation(ViewConfig.Operator.COUNT, "answer")
                )
        ),
        group_by_answers("responseDistributionResponse",
                DataView.ChartType.BAR,
                new ViewConfig(
                        List.of(),
                        ViewConfig.Aggregation.STUDY_GROUP,
                        ViewConfig.Aggregation.TERM_FIELD,
                        new ViewConfig.Operation(ViewConfig.Operator.COUNT, "answer")
                )
        ),
        ;

        private final String label;
        private final String title;
        private final String description;
        private final DataView.ChartType chartType;

        private final ViewConfig viewConfig;

        DataViewInfoType(String i18nKey, DataView.ChartType chartType, ViewConfig viewConfig) {
            this(
                    "data.charts.simpleQuestion.%s.label".formatted(i18nKey),
                    "data.charts.simpleQuestion.%s.title".formatted(i18nKey),
                    "data.charts.simpleQuestion.%s.description".formatted(i18nKey),
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

        @Override
        public DataView.ChartType chartType() {
            return chartType;
        }

        public ViewConfig getViewConfig() {
            return viewConfig;
        }
    }

    @Override
    public Set<DataViewInfo> listViews() {
        return Stream.of(DataViewInfoType.values())
                .collect(Collectors.toSet());
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
