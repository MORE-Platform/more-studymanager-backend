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
import io.redlink.more.studymanager.core.validation.ConfigurationValidationReport;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class QuestionObservationFactory<C extends Observation<P>, P extends ObservationProperties>
        extends ObservationFactory<C, P> {

    private static final MeasurementSet measurements = new MeasurementSet(
            "SIMPLE_ANSWER", Set.of(new Measurement("answer", Measurement.Type.STRING))
    );

    private static List<Value> properties = List.of(
        new StringValue("question")
                .setName("Question")
                .setDescription("The question you want to ask")
                .setRequired(true),
            new StringListValue("answers")
                    .setMinSize(2)
                    .setMaxSize(5)
                    .setName("Answers")
                    .setDescription("Possible answers (min 2, max 5")
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
        return "Question Observation";
    }

    @Override
    public String getDescription() {
        return "This observation enables you to create a simple MultipleChoice question.";
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

    public Boolean getDefaultHidden() { return false; }

    public Boolean getHidden(Boolean hidden) { return getDefaultHidden(); }
}
