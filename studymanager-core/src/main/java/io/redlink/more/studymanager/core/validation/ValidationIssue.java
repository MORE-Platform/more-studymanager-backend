package io.redlink.more.studymanager.core.validation;

import io.redlink.more.studymanager.core.properties.model.Value;

public class ValidationIssue {
    public enum Type {
        None,
        ERROR,
        WARNING
    }

    public static ValidationIssue NONE = new ValidationIssue(Type.None, null, null);

    private Type type;
    private String propertyId;
    private String message;

    private ValidationIssue(Type type, String propertyId, String message) {
        this.type = type;
        this.propertyId = propertyId;
        this.message = message;
    }

    public static boolean nonNone(ValidationIssue issue) {
        return issue != null && Type.None != issue.type;
    }

    public static ValidationIssue error(Value value, String message) {
        return new ValidationIssue(Type.ERROR, value.getId(), message);
    }

    public static ValidationIssue warning(Value value, String message) {
        return new ValidationIssue(Type.WARNING, value.getId(), message);
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
