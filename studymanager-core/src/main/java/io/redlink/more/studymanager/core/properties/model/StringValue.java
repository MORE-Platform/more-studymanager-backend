package io.redlink.more.studymanager.core.properties.model;

public class StringValue extends Value<String> {
    public StringValue(String id) {
        super(id);
    }

    @Override
    public String getType() {
        return "STRING";
    }

    @Override
    public Class<String> getValueType() {
        return String.class;
    }
}
