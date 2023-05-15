package io.redlink.more.studymanager.core.properties.model;

import java.util.List;

public class ChoiceValue extends Value<String> {
    private List<String> options;

    public ChoiceValue(String id) {
        super(id);
    }

    @Override
    public String getType() {
        return "CHOICE";
    }

    @Override
    public Class<String> getValueType() {
        return String.class;
    }
}
