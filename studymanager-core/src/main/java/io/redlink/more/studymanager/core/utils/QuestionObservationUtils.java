package io.redlink.more.studymanager.core.utils;

import io.redlink.more.studymanager.core.datavalidity.MeasurementSummary;
import io.redlink.more.studymanager.core.datavalidity.ObservationDataState;
import io.redlink.more.studymanager.core.datavalidity.ObservationDataSummary;
import io.redlink.more.studymanager.core.datavalidity.ObservationValidationResult;
import io.redlink.more.studymanager.core.ui.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class QuestionObservationUtils {

    private QuestionObservationUtils() {}

    public enum DataViewInfoType {
        response_distribution,
        answers_by_group,
        group_by_answers
    }

    public static class SimpleDataViewInfo implements DataViewInfo {
        private final String name;
        private final String label;
        private final String title;
        private final String description;
        private final DataView.ChartType chartType;
        private final ViewConfig viewConfig;

        public SimpleDataViewInfo(String name, String label, String title, String description, DataView.ChartType chartType, ViewConfig viewConfig) {
            this.name = name;
            this.label = label;
            this.title = title;
            this.description = description;
            this.chartType = chartType;
            this.viewConfig = viewConfig;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String label() {
            return label;
        }

        @Override
        public String title() {
            return title;
        }

        @Override
        public String description() {
            return description;
        }

        @Override
        public DataView.ChartType chartType() {
            return chartType;
        }

        public ViewConfig getViewConfig() {
            return viewConfig;
        }
    }

    public static List<DataViewInfo> createDefaultQuestionDataViews(String i18nPrefix, String fieldId) {
        return List.of(
                new SimpleDataViewInfo(
                        DataViewInfoType.response_distribution.name(),
                        "%s.responseDistribution.label".formatted(i18nPrefix),
                        "%s.responseDistribution.title".formatted(i18nPrefix),
                        "%s.responseDistribution.description".formatted(i18nPrefix),
                        DataView.ChartType.PIE,
                        new ViewConfig(
                                List.of(),
                                null,
                                ViewConfig.Aggregation.TERM_FIELD,
                                new ViewConfig.Operation(ViewConfig.Operator.COUNT, fieldId)
                        )
                ),
                new SimpleDataViewInfo(
                        DataViewInfoType.answers_by_group.name(),
                        "%s.responseDistributionStudyGroup.label".formatted(i18nPrefix),
                        "%s.responseDistributionStudyGroup.title".formatted(i18nPrefix),
                        "%s.responseDistributionStudyGroup.description".formatted(i18nPrefix),
                        DataView.ChartType.BAR,
                        new ViewConfig(
                                List.of(),
                                ViewConfig.Aggregation.TERM_FIELD,
                                ViewConfig.Aggregation.STUDY_GROUP,
                                new ViewConfig.Operation(ViewConfig.Operator.COUNT, fieldId)
                        )
                ),
                new SimpleDataViewInfo(
                        DataViewInfoType.group_by_answers.name(),
                        "%s.responseDistributionResponse.label".formatted(i18nPrefix),
                        "%s.responseDistributionResponse.title".formatted(i18nPrefix),
                        "%s.responseDistributionResponse.description".formatted(i18nPrefix),
                        DataView.ChartType.BAR,
                        new ViewConfig(
                                List.of(),
                                ViewConfig.Aggregation.STUDY_GROUP,
                                ViewConfig.Aggregation.TERM_FIELD,
                                new ViewConfig.Operation(ViewConfig.Operator.COUNT, fieldId)
                        )
                )
        );
    }

    public static DataViewData addMissingAnswerOptionsHelper(
            DataViewInfoType dataViewInfoType,
            DataViewData dataViewData,
            List<String> allPossibleAnswerOptions
    ) {
        List<String> labels = new ArrayList<>(dataViewData.labels());
        List<DataViewRow> rows = new ArrayList<>(dataViewData.rows());

        switch (dataViewInfoType) {
            case response_distribution -> {
                if (allPossibleAnswerOptions.size() == labels.size() || labels.isEmpty()) {
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
            }

            case answers_by_group -> {
                if (allPossibleAnswerOptions.size() == rows.size() || rows.isEmpty()) {
                    return dataViewData;
                }
                for (String item : allPossibleAnswerOptions) {
                    if (rows.stream().noneMatch(v -> v.label().equals(item))) {
                        ArrayList<Double> values = Stream.generate(() -> (Double) null)
                                .limit(labels.size())
                                .collect(Collectors.toCollection(ArrayList::new));

                        rows.add(new DataViewRow(item, values));
                    }
                }
            }

            case group_by_answers -> {
                if (allPossibleAnswerOptions.size() == labels.size() || labels.isEmpty()) {
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
            }

            default -> {
                return dataViewData;
            }
        }

        return new DataViewData(labels, rows);
    }

    public static ObservationValidationResult validateSingleAnswerObservation(
            ObservationDataSummary observationDataSummary,
            String fieldId
    ) {
        if (observationDataSummary == null) {
            return new ObservationValidationResult(false, ObservationDataState.MISSING);
        }
        if (observationDataSummary.numDocs() <= 0) {
            return new ObservationValidationResult(false, ObservationDataState.MISSING);
        } else if (observationDataSummary.numDocs() > 1) {
            return new ObservationValidationResult(true, ObservationDataState.COMPLETE);
        } else {
            MeasurementSummary answerMeasurementSummary = observationDataSummary.measurements().stream()
                    .filter(it -> fieldId.equals(it.getMeasurement().getId()))
                    .findFirst()
                    .orElse(null);
            boolean hasAnswers = answerMeasurementSummary != null &&
                    answerMeasurementSummary.getStringResult().values().stream().noneMatch(it -> it.value() == null);
            return new ObservationValidationResult(!hasAnswers, hasAnswers ? ObservationDataState.COMPLETE : ObservationDataState.MISSING);
        }
    }
}