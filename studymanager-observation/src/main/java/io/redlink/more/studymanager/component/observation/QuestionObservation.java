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

public class QuestionObservation<C extends ObservationProperties> extends Observation<C> {

    public QuestionObservation(MoreObservationSDK sdk, C properties) throws ConfigurationValidationException {
        super(sdk, properties);
    }

    private enum DataViewInfoType {
        QUESTIONS("questions", "Questions", "All answers summed up."),
        ANSWERS_BY_GROUP("answersByGroup", "Questions grouped", "All answers summed by group."),
        GROUP_BY_ANSWERS("groupByAnswers", "Questions answers", "All answers summed by answers.");

        private final DataViewInfo dataViewInfo;

        DataViewInfoType(String key, String displayName, String description) {
            this.dataViewInfo = new DataViewInfo(key, displayName, description);
        }

        public DataViewInfo getDataViewInfo() {
            return dataViewInfo;
        }
    }

    @Override
    public Set<DataViewInfo> listViews() {
        return Stream.of(DataViewInfoType.values())
                .map(DataViewInfoType::getDataViewInfo)
                .collect(Collectors.toSet());
    }

    @Override
    public DataView getView(String viewId, Integer studyGroupId, Integer participantId, TimeRange timerange) {
        return switch (viewId) {
            case "questions" -> createQuestionsView(studyGroupId, participantId, timerange);
            case "answersByGroup" -> createAnswersByGroupView(studyGroupId, participantId, timerange);
            case "groupByAnswers" -> createGroupByAnswersView(studyGroupId, participantId, timerange);
            default -> null;
        };
    }

    private DataView createQuestionsView(Integer studyGroupId, Integer participantId, TimeRange timerange) {
        var viewConfig = new ViewConfig(
                List.of(),
                null,
                null,
                new ViewConfig.Operation(ViewConfig.Operator.COUNT, "answer")
        );

        List<DataViewRow> rows = sdk.queryData(viewConfig, participantId, timerange);

        return new DataView(
                DataViewInfoType.QUESTIONS.getDataViewInfo(),
                DataView.ChartType.PIE,
                List.of(),
                rows
        );
    }

    private DataView createAnswersByGroupView(Integer studyGroupId, Integer participantId, TimeRange timerange) {
        var viewConfig = new ViewConfig(
                List.of(),
                null,
                ViewConfig.Aggregation.STUDY_GROUP,
                new ViewConfig.Operation(ViewConfig.Operator.COUNT, "answer")
        );

        List<DataViewRow> rows = sdk.queryData(viewConfig, participantId, timerange);
        // TODO nitber: make chart type configurable by func parameter
        return new DataView(
                DataViewInfoType.QUESTIONS.getDataViewInfo(),
                DataView.ChartType.BAR,
                List.of(),
                rows
        );
    }

    private DataView createGroupByAnswersView(Integer studyGroupId, Integer participantId, TimeRange timerange) {
        var viewConfig = new ViewConfig(
                List.of(),
                ViewConfig.Aggregation.STUDY_GROUP,
                null,
                new ViewConfig.Operation(ViewConfig.Operator.COUNT, "answer")
        );

        List<DataViewRow> rows = sdk.queryData(viewConfig, participantId, timerange);
        // TODO nitber: make chart type configurable by func parameter
        return new DataView(
                DataViewInfoType.QUESTIONS.getDataViewInfo(),
                DataView.ChartType.BAR,
                List.of(),
                rows
        );
    }
}
