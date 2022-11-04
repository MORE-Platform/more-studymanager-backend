package io.redlink.more.studymanager.core.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigurationValidationReport {

    public static final ConfigurationValidationReport VALID = ConfigurationValidationReport.init();
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

    @Override
    public String toString() {
        return "ConfigurationValidationReport: " +
                issues.stream().map(i -> "[" + i.getType() + "] " + i.getMessage())
                        .collect(Collectors.joining("; "));
    }
}
