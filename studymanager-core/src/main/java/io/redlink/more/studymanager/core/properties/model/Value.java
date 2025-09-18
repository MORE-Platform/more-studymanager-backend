/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.core.properties.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.redlink.more.studymanager.core.exception.ValueCastException;
import io.redlink.more.studymanager.core.exception.ValueNonNullException;
import io.redlink.more.studymanager.core.properties.ComponentProperties;
import io.redlink.more.studymanager.core.validation.ValidationIssue;
import java.util.function.Function;

public abstract class Value<T> {
    private final String id;
    private String name;
    private String description;
    private T defaultValue;
    private boolean required = false;
    private boolean immutable = false;
    private Function<T, ValidationIssue> validationFunction = (T t) -> ValidationIssue.NONE;

    public Value(String id) {
        this.id = id;
    }

    public final ValidationIssue validate(T t) {
        if (required) {
            if (t == null) {
                return ValidationIssue.requiredMissing(this);
            }
        }

        final ValidationIssue subResult = doValidate(t);
        if (subResult != null && subResult.getType() != ValidationIssue.Type.None) {
            return subResult;
        }

        final ValidationIssue result = validationFunction.apply(t);
        if (result == null) {
            return ValidationIssue.NONE;
        } else {
            return result;
        }
    }

    protected ValidationIssue doValidate(T t) {
        return ValidationIssue.NONE;
    }

    public T getValue(ComponentProperties properties) {
        if (properties.containsKey(id)) {
            try {
                return getValueType().cast(properties.get(id));
            } catch (ClassCastException e) {
                throw new ValueCastException(this, getValueType());
            }
        } else {
            if (required && defaultValue == null) {
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

    public Value<T> setValidationFunction(Function<T, ValidationIssue> validationFunction) {
        this.validationFunction = validationFunction;
        return this;
    }
}
