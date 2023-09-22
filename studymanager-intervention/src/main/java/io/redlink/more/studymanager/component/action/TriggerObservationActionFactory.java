package io.redlink.more.studymanager.component.action;

import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ActionFactory;
import io.redlink.more.studymanager.core.properties.ActionProperties;
import io.redlink.more.studymanager.core.properties.model.*;
import io.redlink.more.studymanager.core.sdk.MoreActionSDK;

import java.util.List;

public class TriggerObservationActionFactory extends ActionFactory<TriggerObservationAction, ActionProperties> {
    private static List<Value> properties = List.of(
            new StringValue("title")
                    .setRequired(true),
            new StringTextValue("message")
                    .setRequired(true),
            new TriggerObservationValue("observation")
                    .setRequired(true)
                    .setDescription("intervention.factory.actions.triggerObservation.properties.observation.description")
                    .setName("intervention.factory.actions.triggerObservation.properties.observation.name")
    );
    @Override
    public TriggerObservationAction create(MoreActionSDK sdk, ActionProperties properties) throws ConfigurationValidationException {
        return new TriggerObservationAction(sdk, validate(properties));
    }

    @Override
    public String getId() {
        return "trigger-observation-action";
    }

    @Override
    public String getTitle() {
        return "intervention.factory.actions.triggerObservation.title";
    }

    @Override
    public String getDescription() {
        return "intervention.factory.actions.triggerObservation.description";
    }

    @Override
    public List<Value> getProperties() {
        return properties;
    }
}
