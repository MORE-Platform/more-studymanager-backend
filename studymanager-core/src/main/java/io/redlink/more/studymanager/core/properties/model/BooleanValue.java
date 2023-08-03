package io.redlink.more.studymanager.core.properties.model;

public class BooleanValue extends Value<Boolean> {
    public BooleanValue(String id) {
        super(id);
    }

    @Override
    public Class<Boolean> getValueType() {
        return Boolean.class;
    }

    @Override
    public String getType() {
        return "BOOLEAN";
    }
}
