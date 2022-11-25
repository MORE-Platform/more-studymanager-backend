package io.redlink.more.studymanager.component.observation;

import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MorePlatformSDK;
import io.redlink.more.studymanager.core.validation.ConfigurationValidationReport;

import java.util.List;

public class QuestionObservationFactory<C extends Observation, P extends ObservationProperties>
        extends ObservationFactory<C, P> {
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
        return
"""
This observation allows to get create a simple MultipleChoice question. Parameters:
<code>
{
    "question": "Are you fine?"
    "answers": [
        "No",
        "Yes
    ]
}
</code>
""";
    }

    @Override
    public ObservationProperties validate(ObservationProperties properties) {
        ConfigurationValidationReport report = ConfigurationValidationReport.init();
        if(!properties.containsKey("question")) {
            report.missingProperty("question");
        }
        if(!properties.containsKey("answers")) {
            report.missingProperty("answers");
        }
        if(properties.containsKey("answers") && !List.class.isAssignableFrom(properties.get("answers").getClass())) {
            report.error("Value of answers must be a list");
        }
        if(properties.containsKey("answers") && ((List)properties.get("answers")).size() < 2) {
            report.warning("Value answers must contain at least 2 values");
        }
        if(report.isValid()) {
            return properties;
        } else {
            throw new ConfigurationValidationException(report);
        }
    }

    @Override
    public QuestionObservation create(MorePlatformSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new QuestionObservation(sdk, validate(properties));
    }
}
