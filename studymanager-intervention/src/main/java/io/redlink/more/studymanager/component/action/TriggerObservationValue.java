package io.redlink.more.studymanager.component.action;

import io.redlink.more.studymanager.core.properties.model.ObjectValue;

public class TriggerObservationValue extends ObjectValue {
    public TriggerObservationValue(String id) {
        super(id);
    }

    @Override
    public String getType() {
        return "OBSERVATION";
    }
}
