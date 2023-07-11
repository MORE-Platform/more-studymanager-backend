package io.redlink.more.studymanager.core.properties.model;

public class StringTextValue extends Value<String> {
    public StringValue(String id) {
        super(id);
    }

    @Override
    public String getType() {
        return "STRINGTEXT";
    }

    @Override
    public Class<String> getValueType() {
        return String.class;
    }
}
