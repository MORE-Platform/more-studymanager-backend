package io.redlink.more.studymanager.component.observation;

import com.fasterxml.jackson.databind.JsonNode;
import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ApiCallException;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ComponentFactory;
import io.redlink.more.studymanager.core.factory.ComponentFactoryProperties;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.model.User;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MorePlatformSDK;
import io.redlink.more.studymanager.core.validation.ConfigurationValidationReport;

import java.util.Map;
import java.util.Objects;

public class LimeSurveyObservationFactory<C extends Observation, P extends ObservationProperties>
        extends ObservationFactory<C, P> {

    private static final String ID_PROPERTY = "limeSurveyId";

    private LimeSurveyRequestService limeSurveyRequestService;


    @Override
    public ComponentFactory init(ComponentFactoryProperties componentProperties){
        this.componentProperties = componentProperties;
        limeSurveyRequestService = new LimeSurveyRequestService(componentProperties);
        return this;
    }

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
        return Map.of(ID_PROPERTY, "limeSurveyObservation");
    }
    @Override
    public ObservationProperties validate(ObservationProperties properties) {
        ConfigurationValidationReport report = ConfigurationValidationReport.init();
        if(!properties.containsKey(ID_PROPERTY))
            report.missingProperty(ID_PROPERTY);
        if(report.isValid())
            return properties;
        else
            throw new ConfigurationValidationException(report);
    }

    @Override
    public LimeSurveyObservation create(MorePlatformSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new LimeSurveyObservation(sdk, validate(properties), limeSurveyRequestService);
    }

    @Override
    public JsonNode handleAPICall(String slug, User user, JsonNode input) throws ApiCallException {
        LimeSurveyRequestService client = LimeSurveyRequestService.getInstance();
        String filter = input.get("filter") != null ? input.get("filter").asText() : null;
        Integer size = input.get("size") != null ? input.get("size").asInt() : null;
        Integer start = input.get("start") != null ? input.get("start").asInt() : null;
        if (Objects.equals(slug, "surveys")) {
            JsonNode response;
            response = client.listSurveysByUser(user.username(), filter, start, size);
            if (response.get("error") != null) {
                throw new ApiCallException(400, response.get("error").asText());
            }
            return response;
        } else {
            throw new ApiCallException(404, "Not found");
        }
    }
}
