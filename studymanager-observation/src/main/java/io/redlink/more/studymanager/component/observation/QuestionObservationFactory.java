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

public class QuestionObservationFactory<C extends Observation<P>, P extends ObservationProperties>
        extends ObservationFactory<C, P> {

    private static final MeasurementSet measurements = new MeasurementSet(
            "SIMPLE_ANSWER", Set.of(new Measurement("answer", Measurement.Type.STRING))
    );

    private static List<Value> properties = List.of(
        new StringValue("question")
                .setName("observation.factory.simpleQuestion.configProps.questionName")
                .setDescription("observation.factory.simpleQuestion.configProps.questionDesc")
                .setRequired(true),
            new StringListValue("answers")
                    .setMinSize(2)
                    .setMaxSize(5)
                    .setName("observation.factory.simpleQuestion.configProps.answerName")
                    .setDescription("observation.factory.simpleQuestion.configProps.answerDesc")
                    .setDefaultValue(List.of(
                            "No",
                            "Yes"
                    ))
    );
    @Override
    public String getId() {
        return "question-observation";
    }

    @Override
    public String getTitle() {
        return "observation.factory.simpleQuestion.title";
    }

    @Override
    public String getDescription() {
        return "observation.factory.simpleQuestion.description";
    }

    public List<Value> getProperties() {
        return properties;
    }

    @Override
    public QuestionObservation create(MoreObservationSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new QuestionObservation(sdk, validate((P)properties));
    }

    @Override
    public MeasurementSet getMeasurementSet() {
        return measurements;
    }
}
