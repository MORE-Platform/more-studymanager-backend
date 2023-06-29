package io.redlink.more.studymanager.core.ui;

public class OptionValue {
    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public OptionValue setName(String name) {
        this.name = name;
        return this;
    }

    public String getValue() {
        return value;
    }

    public OptionValue setValue(String value) {
        this.value = value;
        return this;
    }
}
