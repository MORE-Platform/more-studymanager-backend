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
        response_distribution("data.charts.simpleQuestion.responseDistribution.label", "data.charts.simpleQuestion.responseDistribution.title", "data.charts.simpleQuestion.responseDistribution.description"),
        answers_by_group("data.charts.simpleQuestion.responseDistributionStudyGroup.label", "data.charts.simpleQuestion.responseDistributionStudyGroup.title", "data.charts.simpleQuestion.responseDistributionStudyGroup.description"),
        group_by_answers("data.charts.simpleQuestion.responseDistributionResponse.label", "data.charts.simpleQuestion.responseDistributionResponse.title", "data.charts.simpleQuestion.responseDistributionResponse.description");

        private final String label;
        private final String title;
        private final String description;

        DataViewInfoType(String label, String title, String description) {
            this.label = label;
            this.title = title;
            this.description = description;
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
    }

    @Override
    public Set<DataViewInfo> listViews() {
        return Stream.of(DataViewInfoType.values())
                .collect(Collectors.toSet());
    }

    @Override
    public DataView getView(String viewName, Integer studyGroupId, Integer participantId, TimeRange timerange) {
        return switch (DataViewInfoType.valueOf(viewName)) {
            case response_distribution -> createQuestionsView(studyGroupId, participantId, timerange);
            case answers_by_group -> createAnswersByGroupView(studyGroupId, participantId, timerange);
            case group_by_answers -> createGroupByAnswersView(studyGroupId, participantId, timerange);
        };
    }

    private DataView createQuestionsView(Integer studyGroupId, Integer participantId, TimeRange timerange) {
        var viewConfig = new ViewConfig(
                List.of(),
                null,
                ViewConfig.Aggregation.TERM_FIELD,
                new ViewConfig.Operation(ViewConfig.Operator.COUNT, "answer")
        );

        DataViewData dataViewData = sdk.queryData(viewConfig, studyGroupId, participantId, timerange);

        return new DataView(
                DataViewInfoType.response_distribution,
                DataView.ChartType.PIE,
                dataViewData
        );
    }

    private DataView createAnswersByGroupView(Integer studyGroupId, Integer participantId, TimeRange timerange) {
        var viewConfig = new ViewConfig(
                List.of(),
                ViewConfig.Aggregation.TERM_FIELD,
                ViewConfig.Aggregation.STUDY_GROUP,
                new ViewConfig.Operation(ViewConfig.Operator.COUNT, "answer")
        );

        DataViewData dataViewData = sdk.queryData(viewConfig, studyGroupId, participantId, timerange);

        return new DataView(
                DataViewInfoType.answers_by_group,
                DataView.ChartType.BAR,
                dataViewData
        );
    }

    private DataView createGroupByAnswersView(Integer studyGroupId, Integer participantId, TimeRange timerange) {
        var viewConfig = new ViewConfig(
                List.of(),
                ViewConfig.Aggregation.STUDY_GROUP,
                ViewConfig.Aggregation.TERM_FIELD,
                new ViewConfig.Operation(ViewConfig.Operator.COUNT, "answer")
        );

        DataViewData dataViewData = sdk.queryData(viewConfig, studyGroupId, participantId, timerange);
        return new DataView(
                DataViewInfoType.group_by_answers,
                DataView.ChartType.BAR,
                dataViewData
        );
    }
}
