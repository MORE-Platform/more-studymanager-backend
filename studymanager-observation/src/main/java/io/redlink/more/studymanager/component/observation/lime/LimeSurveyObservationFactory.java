/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.component.observation.lime;

import com.fasterxml.jackson.databind.JsonNode;
import io.redlink.more.studymanager.component.observation.measurement.GenericMeasurementSets;
import io.redlink.more.studymanager.core.exception.ApiCallException;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ComponentFactoryProperties;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.measurement.MeasurementSet;
import io.redlink.more.studymanager.core.model.User;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.properties.model.StringValue;
import io.redlink.more.studymanager.core.properties.model.Value;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;
import java.util.List;
import java.util.Optional;

public class LimeSurveyObservationFactory<C extends LimeSurveyObservation<P>, P extends ObservationProperties>
        extends ObservationFactory<C, P> {

    public static final Value<String> limeSurveyId = new StringValue("limeSurveyId")
            .setName("observation.factory.limeSurvey.configProps.idName")
            .setDescription("observation.factory.limeSurvey.configProps.idDesc")
            .setRequired(true)
            .setImmutable(true);

    private static final List<Value> properties = List.of(
            /* TODO enable Autocomplete in FE
            new AutocompleteValue("limeSurveyId", "surveys")
                    .setName("Survey")
                    .setDescription("An existing survey")
                    .setRequired(true)
             */
            limeSurveyId
    );

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
        return "observation.factory.limeSurvey.title";
    }

    @Override
    public String getDescription() {
        return "observation.factory.limeSurvey.description";
    }

    @Override
    public List<Value> getProperties() {
        return properties;
    }

    @Override
    public LimeSurveyObservation<ObservationProperties> create(MoreObservationSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new LimeSurveyObservation(sdk, validate((P)properties), limeSurveyRequestService);
    }

    @Override
    public ObservationProperties preImport(ObservationProperties properties) {
        properties.remove(LimeSurveyObservation.LIME_SURVEY_ID);
        return properties;
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

    @Override
    public MeasurementSet getMeasurementSet() {
        return GenericMeasurementSets.NOT_SPECIFIED;
    }
}
