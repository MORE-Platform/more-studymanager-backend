package io.redlink.more.studymanager.component.trigger.datacheck;

import io.redlink.more.studymanager.core.properties.model.ObjectValue;

public class DatacheckQueryValue extends ObjectValue {
    public DatacheckQueryValue(String id) {
        super(id);
    }

    @Override
    public String getType() {
        return "DATACHECKQUERY";
    }
}
