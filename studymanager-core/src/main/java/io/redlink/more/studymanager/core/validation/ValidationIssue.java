package io.redlink.more.studymanager.core.validation;

public class ValidationIssue {
    public enum Type {
        ERROR,
        WARNING
    }

    private Type type;
    private String message;

    private ValidationIssue(Type type, String message) {
        this.type = type;
        this.message = message;
    }

    public static ValidationIssue error(String message) {
        return new ValidationIssue(Type.ERROR, message);
    }

    public static ValidationIssue warning(String message) {
        return new ValidationIssue(Type.WARNING, message);
    }

    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
