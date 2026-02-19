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
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.measurement.Measurement;
import io.redlink.more.studymanager.core.measurement.MeasurementSet;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.properties.model.StringListValue;
import io.redlink.more.studymanager.core.properties.model.StringValue;
import io.redlink.more.studymanager.core.properties.model.Value;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;

import java.util.List;
import java.util.Set;

public class MultipleChoiceQuestionObservationFactory<C extends Observation<P>, P extends ObservationProperties>
        extends ObservationFactory<C, P> {

    public static final String FIELD_ANSWERS =  "answers";

    private static final MeasurementSet measurements = new MeasurementSet(
            "MULTIPLE_CHOICE_ANSWERS", Set.of(new Measurement(FIELD_ANSWERS, Measurement.Type.STRING_ARRAY))
    );

    private static List<Value> properties = List.of(
        new StringValue("question")
                .setName("observation.factory.multipleChoiceQuestion.configProps.questionName")
                .setDescription("observation.factory.multipleChoiceQuestion.configProps.questionDesc")
                .setRequired(true),
            new StringListValue("answers")
                    .setMinSize(2)
                    .setMaxSize(10)
                    .setName("observation.factory.multipleChoiceQuestion.configProps.answerName")
                    .setDescription("observation.factory.multipleChoiceQuestion.configProps.answerDesc")
                    .setDefaultValue(List.of(
                            "Migrane",
                            "Dizzyness"
                    ))
    );

    @Override
    public String getId() {
        return "multiple-choice-question-observation";
    }

    @Override
    public String getTitle() {
        return "observation.factory.multipleChoiceQuestion.title";
    }

    @Override
    public String getDescription() {
        return "observation.factory.multipleChoiceQuestion.description";
    }

    public List<Value> getProperties() {
        return properties;
    }

    @Override
    public MultipleChoiceQuestionObservation create(MoreObservationSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new MultipleChoiceQuestionObservation(sdk, validate((P)properties));
    }

    @Override
    public MeasurementSet getMeasurementSet() {
        return measurements;
    }
}
