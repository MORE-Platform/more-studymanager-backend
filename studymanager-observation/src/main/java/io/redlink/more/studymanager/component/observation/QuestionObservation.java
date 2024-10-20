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

import java.util.ArrayList;
import java.util.List;
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
                    "monitoring.charts.simpleQuestion.%s.label".formatted(i18nKey),
                    "monitoring.charts.simpleQuestion.%s.title".formatted(i18nKey),
                    "monitoring.charts.simpleQuestion.%s.description".formatted(i18nKey),
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
                addMissingAnswerOptions(dataView, dataViewData)
        );

    }

    private DataViewData addMissingAnswerOptions(DataViewInfoType dataView, DataViewData dataViewData) {
        ArrayList<String> allPossibleAnswerOptions = (ArrayList<String>) this.properties.get("answers");
        List<String> labels = new ArrayList<>(dataViewData.labels());
        List<DataViewRow> rows = new ArrayList<>(dataViewData.rows());

        switch (dataView) {
            case response_distribution:
                if (allPossibleAnswerOptions.size() == dataViewData.labels().size() || dataViewData.labels().isEmpty()) {
                    return dataViewData;
                }
                for (String item : allPossibleAnswerOptions) {
                    if (!labels.contains(item)) {
                        labels.add(item);
                        if (!rows.isEmpty()) {
                            rows.get(0).values().add(0.0);
                        }
                    }
                }
                break;
            case answers_by_group:
                if (allPossibleAnswerOptions.size() == dataViewData.rows().size() || dataViewData.rows().isEmpty()) {
                    return dataViewData;
                }
                for (String item : allPossibleAnswerOptions) {
                    if (rows.stream().noneMatch(v -> v.label().equals(item))) {
                        ArrayList<Double> values = Stream.generate(() -> (Double) null)
                                .limit(labels.size()).collect(Collectors.toCollection(ArrayList::new));

                        rows.add(new DataViewRow(item, values));
                    }
                }
                break;
            case group_by_answers:
                if (allPossibleAnswerOptions.size() == dataViewData.labels().size() || dataViewData.labels().isEmpty()) {
                    return dataViewData;
                }

                for (String item : allPossibleAnswerOptions) {
                    if (!labels.contains(item)) {
                        labels.add(item);
                        for (DataViewRow row : rows) {
                            row.values().add(0.0);
                        }
                    }
                }
                break;
        }

        return new DataViewData(labels, rows);
    }
}
