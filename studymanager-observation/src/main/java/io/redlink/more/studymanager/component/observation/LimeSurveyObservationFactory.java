package io.redlink.more.studymanager.component.observation;

import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MorePlatformSDK;
import io.redlink.more.studymanager.core.validation.ConfigurationValidationReport;

import java.util.Map;

public class LimeSurveyObservationFactory<C extends Observation, P extends ObservationProperties>
        extends ObservationFactory<C, P> {

    @Override
    public String getId() {
        return "lime-survey-observation";
    }

    @Override
    public String getTitle() {
        return "Lime Survey Observation";
    }

    @Override
    public String getDescription() {
        return "This observation enables you to create a Lime Survey questionnaire";
    }

    @Override
    public Map<String, Object> getDefaultProperties(){
        return Map.of("limeSurveyId", "limeSurveyObservation");
    }
    @Override
    public ObservationProperties validate(ObservationProperties properties) {
        ConfigurationValidationReport report = ConfigurationValidationReport.init();
        if(!properties.containsKey("limeSurveyId"))
            report.missingProperty("limeSurveyId");
        if(report.isValid())
            return properties;
        else
            throw new ConfigurationValidationException(report);
    }

    @Override
    public LimeSurveyObservation create(MorePlatformSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new LimeSurveyObservation(sdk, validate(properties));
    }
}
