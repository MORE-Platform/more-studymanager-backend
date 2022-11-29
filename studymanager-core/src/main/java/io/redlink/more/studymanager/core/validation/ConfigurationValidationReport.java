package io.redlink.more.studymanager.core.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigurationValidationReport {
    private final List<ValidationIssue> issues;

    private ConfigurationValidationReport() {
        this.issues = new ArrayList<>();
    }

    public static ConfigurationValidationReport init() {
        return new ConfigurationValidationReport();
    }

    public ConfigurationValidationReport error(String message) {
        this.issues.add(ValidationIssue.error(message));
        return this;
    }

    public ConfigurationValidationReport warning(String message) {
        this.issues.add(ValidationIssue.warning(message));
        return this;
    }

    public ConfigurationValidationReport missingProperty(String property) {
        return error("Property '" + property + "' must be set");
    }

    public boolean isValid() {
        return this.listIssues().size() == 0;
    }
    List<ValidationIssue> listIssues() {
        return issues;
    }

    public List<ValidationIssue> getIssues() {
        return issues;
    }

    public List<ValidationIssue> getErrors() {
        return this.issues.stream().filter(i -> i.getType().equals(ValidationIssue.Type.ERROR)).toList();
    }

    public List<ValidationIssue> getWarnings() {
        return this.issues.stream().filter(i -> i.getType().equals(ValidationIssue.Type.WARNING)).toList();
    }

    @Override
    public String toString() {
        return "ConfigurationValidationReport: " +
                issues.stream().map(i -> "[" + i.getType() + "] " + i.getMessage())
                        .collect(Collectors.joining("; "));
    }
}
