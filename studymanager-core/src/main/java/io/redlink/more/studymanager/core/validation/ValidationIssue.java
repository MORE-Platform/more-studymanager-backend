/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.core.validation;

import io.redlink.more.studymanager.core.properties.model.Value;
import java.util.Optional;

public class ValidationIssue {
    public enum Type {
        None,
        ERROR,
        WARNING
    }

    public static ValidationIssue NONE = new ValidationIssue(Type.None, null, null);

    private final Type type;
    private final String propertyId;
    private final String message;

    private ValidationIssue(Type type, String propertyId, String message) {
        this.type = type;
        this.propertyId = propertyId;
        this.message = message;
    }

    public static boolean nonNone(ValidationIssue issue) {
        return issue != null && Type.None != issue.type;
    }

    public static ValidationIssue error(Value<?> value, String message) {
        return new ValidationIssue(Type.ERROR, Optional.ofNullable(value).map(Value::getId).orElse(null), message);
    }

    public static ValidationIssue warning(Value<?> value, String message) {
        return new ValidationIssue(Type.WARNING, Optional.ofNullable(value).map(Value::getId).orElse(null), message);
    }

    public static ValidationIssue requiredMissing(Value<?> value) {
        return error(value, "global.error.required");
    }

    public static ValidationIssue immutablePropertyChanged(Value<?> value) {
        return error(value, "global.error.immutable");
    }

    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getPropertyId() {
        return propertyId;
    }
}
