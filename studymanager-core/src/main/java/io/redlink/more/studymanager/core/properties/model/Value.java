package io.redlink.more.studymanager.core.properties.model;

import io.redlink.more.studymanager.core.validation.ValidationIssue;

public abstract class Value<T> {
    private String id;
    private String name;
    private String description;
    private T defaultValue;
    private boolean required = false;
    public ValidationIssue validate(T t) {
        return null;
    }

    public abstract String getType();

    public String getId() {
        return id;
    }

    public Value<T> setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Value<T> setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Value<T> setDescription(String description) {
        this.description = description;
        return this;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public Value<T> setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public boolean isRequired() {
        return required;
    }

    public Value<T> setRequired(boolean required) {
        this.required = required;
        return this;
    }
}
