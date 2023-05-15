package io.redlink.more.studymanager.core.properties.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.redlink.more.studymanager.core.exception.ValueCastException;
import io.redlink.more.studymanager.core.exception.ValueNonNullException;
import io.redlink.more.studymanager.core.properties.ComponentProperties;
import io.redlink.more.studymanager.core.validation.ValidationIssue;

public abstract class Value<T> {
    private String id;
    private String name;
    private String description;
    private T defaultValue;
    private boolean required = false;
    private boolean immutable = false;

    public Value(String id) {
        this.id = id;
    }

    public ValidationIssue validate(T t) {
        return null;
    }

    public T getValue(ComponentProperties properties) {
        if(properties.containsKey(id)) {
            try {
                return getValueType().cast(properties.get(id));
            } catch (ClassCastException e) {
                throw new ValueCastException(this, getValueType());
            }
        } else {
            if(required && defaultValue == null) {
                throw new ValueNonNullException(this);
            } else {
                return defaultValue;
            }
        }
    }

    @JsonIgnore
    public abstract Class<T> getValueType();

    public abstract String getType();

    public String getId() {
        return id;
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

    public boolean isImmutable() {
        return immutable;
    }

    public Value<T> setImmutable(boolean immutable) {
        this.immutable = immutable;
        return this;
    }
}
