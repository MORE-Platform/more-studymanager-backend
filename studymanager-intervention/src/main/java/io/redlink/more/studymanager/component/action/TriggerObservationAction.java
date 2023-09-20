package io.redlink.more.studymanager.component.action;

import com.fasterxml.jackson.core.type.TypeReference;
import io.redlink.more.studymanager.core.component.Action;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.io.ActionParameter;
import io.redlink.more.studymanager.core.properties.ActionProperties;
import io.redlink.more.studymanager.core.sdk.MoreActionSDK;

import java.util.Optional;

public class TriggerObservationAction extends Action<ActionProperties> {

    protected TriggerObservationAction(MoreActionSDK sdk, ActionProperties properties) throws ConfigurationValidationException {
        super(sdk, properties);
    }

    @Override
    public void execute(ActionParameter parameters) {

        TriggerObservation o = this.getObservationProp().get();

        sdk.triggerObservation(
                properties.getString("title"),
                properties.getString("message"),
                o.getFactory(),
                o.getId()
        );
    }

    private Optional<TriggerObservation> getObservationProp() {return this.properties.getObject("observation", new TypeReference<>() {});}
}
