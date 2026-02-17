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
import io.redlink.more.studymanager.core.datavalidity.MeasurementSummary;
import io.redlink.more.studymanager.core.datavalidity.ObservationDataState;
import io.redlink.more.studymanager.core.datavalidity.ObservationDataSummary;
import io.redlink.more.studymanager.core.datavalidity.ObservationValidationResult;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.io.TimeRange;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;
import io.redlink.more.studymanager.core.ui.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultipleChoiceQuestionObservation<C extends ObservationProperties> extends Observation<C> {

    public MultipleChoiceQuestionObservation(MoreObservationSDK sdk, C properties) throws ConfigurationValidationException {
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
                        new ViewConfig.Operation(ViewConfig.Operator.COUNT, MultipleChoiceQuestionObservationFactory.FIELD_ANSWERS)
                )
        ),
        answers_by_group("responseDistributionStudyGroup",
                DataView.ChartType.BAR,
                new ViewConfig(
                        List.of(),
                        ViewConfig.Aggregation.TERM_FIELD,
                        ViewConfig.Aggregation.STUDY_GROUP,
                        new ViewConfig.Operation(ViewConfig.Operator.COUNT, MultipleChoiceQuestionObservationFactory.FIELD_ANSWERS)
                )
        ),
        group_by_answers("responseDistributionResponse",
                DataView.ChartType.BAR,
                new ViewConfig(
                        List.of(),
                        ViewConfig.Aggregation.STUDY_GROUP,
                        ViewConfig.Aggregation.TERM_FIELD,
                        new ViewConfig.Operation(ViewConfig.Operator.COUNT, MultipleChoiceQuestionObservationFactory.FIELD_ANSWERS)
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
                    "monitoring.charts.multipleChoiceQuestion.%s.label".formatted(i18nKey),
                    "monitoring.charts.multipleChoiceQuestion.%s.title".formatted(i18nKey),
                    "monitoring.charts.multipleChoiceQuestion.%s.description".formatted(i18nKey),
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
        List<String> allPossibleAnswerOptions = (List<String>) properties.get("answers");
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

    @Override
    public ObservationValidationResult validateData(Instant start, Instant end, ObservationDataSummary observationDataSummary) {
        if(observationDataSummary == null) { //null indicates some problem. This will cause the check to be repeated
            return new ObservationValidationResult(false, ObservationDataState.MISSING);
        }
        if(observationDataSummary.numDocs() <= 0) { //no data
            return new ObservationValidationResult(false, ObservationDataState.MISSING);
        } else if(observationDataSummary.numDocs() > 1) {
            //only a single answer is allowed
            return new ObservationValidationResult(true, ObservationDataState.COMPLETE);
        } else { //exactly one result
            MeasurementSummary answerMeasurementSummary = observationDataSummary.measurements().stream()
                    .filter(it -> MultipleChoiceQuestionObservationFactory.FIELD_ANSWERS.equals(it.getMeasurement().getId()))
                    .findFirst()
                    .orElse(null);
            boolean hasAnswers = answerMeasurementSummary != null &&
                    answerMeasurementSummary.getStringResult().values().stream().noneMatch(it -> it.value() == null);
            return new ObservationValidationResult(!hasAnswers, hasAnswers ? ObservationDataState.COMPLETE : ObservationDataState.MISSING);
        }
    }
}
