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
import io.redlink.more.studymanager.core.datavalidity.ObservationDataSummary;
import io.redlink.more.studymanager.core.datavalidity.ObservationValidationResult;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.io.TimeRange;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;
import io.redlink.more.studymanager.core.ui.*;
import io.redlink.more.studymanager.core.utils.QuestionObservationUtils;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

public class MultipleChoiceQuestionObservation<C extends ObservationProperties> extends Observation<C> {

    public MultipleChoiceQuestionObservation(MoreObservationSDK sdk, C properties) throws ConfigurationValidationException {
        super(sdk, properties);
    }

    @Override
    public DataViewInfo[] listViews() {
        return QuestionObservationUtils.createDefaultQuestionDataViews("monitoring.charts.multipleChoiceQuestion", MultipleChoiceQuestionObservationFactory.FIELD_ANSWERS).toArray(new DataViewInfo[0]);
    }

    @Override
    public DataView getView(String viewName, Integer studyGroupId, Integer participantId, TimeRange timerange) {
        final QuestionObservationUtils.SimpleDataViewInfo dataView = (QuestionObservationUtils.SimpleDataViewInfo) Stream.of(listViews())
                .filter(it -> it.name().equals(viewName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown view: " + viewName));

        final DataViewData dataViewData = sdk.queryData(dataView.getViewConfig(), studyGroupId, participantId, timerange);

        return new DataView(
                dataView,
                dataView.chartType(),
                addMissingAnswerOptions(dataView, dataViewData)
        );
    }

    private DataViewData addMissingAnswerOptions(QuestionObservationUtils.SimpleDataViewInfo dataView, DataViewData dataViewData) {
        List<String> allPossibleAnswerOptions = (List<String>) properties.get("answers");
        return QuestionObservationUtils.addMissingAnswerOptionsHelper(
                QuestionObservationUtils.DataViewInfoType.valueOf(dataView.name()),
                dataViewData,
                allPossibleAnswerOptions
        );
    }

    @Override
    public ObservationValidationResult validateData(Instant start, Instant end, ObservationDataSummary observationDataSummary) {
        return QuestionObservationUtils.validateSingleAnswerObservation(
                observationDataSummary,
                MultipleChoiceQuestionObservationFactory.FIELD_ANSWERS
        );
    }
}
