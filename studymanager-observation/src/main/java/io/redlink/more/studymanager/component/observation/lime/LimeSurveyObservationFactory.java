package io.redlink.more.studymanager.component.observation.lime;

import com.fasterxml.jackson.databind.JsonNode;
import io.redlink.more.studymanager.core.exception.ApiCallException;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ComponentFactoryProperties;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.model.User;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;
import io.redlink.more.studymanager.core.validation.ConfigurationValidationReport;

import java.util.Map;
import java.util.Optional;

public class LimeSurveyObservationFactory<C extends LimeSurveyObservation<P>, P extends LimeSurveyProperties>
        extends ObservationFactory<C, P> {

    private static final String ID_PROPERTY = "limeSurveyId";

    private LimeSurveyRequestService limeSurveyRequestService;

    public LimeSurveyObservationFactory() {}

    public LimeSurveyObservationFactory(
            ComponentFactoryProperties properties, LimeSurveyRequestService limeSurveyRequestService) {
        this.componentProperties = properties;
        this.limeSurveyRequestService = limeSurveyRequestService;
    }
    @Override
    public LimeSurveyObservationFactory<C, P> init(ComponentFactoryProperties componentProperties){
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
    public LimeSurveyProperties validate(LimeSurveyProperties properties) {
        ConfigurationValidationReport report = ConfigurationValidationReport.init();
        if(!properties.containsKey(ID_PROPERTY))
            report.missingProperty(ID_PROPERTY);
        if(report.isValid()) {
            properties.put("limeUrl", limeSurveyRequestService.getSurveyUrl());
            return properties;
        } else
            throw new ConfigurationValidationException(report);
    }

    @Override
    public LimeSurveyObservation<LimeSurveyProperties> create(MoreObservationSDK sdk, LimeSurveyProperties properties) throws ConfigurationValidationException {
        return new LimeSurveyObservation(sdk, validate(properties), limeSurveyRequestService);
    }

    @Override
    public JsonNode handleAPICall(String slug, User user, JsonNode input) throws ApiCallException {
        String filter = Optional.ofNullable(input.get("filter")).map(JsonNode::asText).orElse(null);
        Integer size = Optional.ofNullable(input.get("size")).map(JsonNode::asInt).orElse(10);
        Integer start = Optional.ofNullable(input.get("start")).map(JsonNode::asInt).orElse(0);
        if ("surveys".equals(slug)) {
            try {
                return limeSurveyRequestService.listSurveysByUser(user.username(), filter, start, size);
            } catch (RuntimeException e) {
                throw new ApiCallException(500, e.getMessage());
            }
        } else {
            throw new ApiCallException(404, "Not found");
        }
    }
}
