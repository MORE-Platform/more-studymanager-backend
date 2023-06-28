package io.redlink.more.studymanager.core.properties.model;

public class ObjectValue extends Value<Object> {
    public ObjectValue(String id) {
        super(id);
    }

    @Override
    public Class<Object> getValueType() {
        return Object.class;
    }

    @Override
    public String getType() {
        return "Object";
    }
}
