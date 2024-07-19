/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.core.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigurationValidationReport {
    private final List<ValidationIssue> issues;

    private ConfigurationValidationReport(List<ValidationIssue> issues) {
        this.issues = new ArrayList<>(issues);
    }

    public static ConfigurationValidationReport init() {
        return new ConfigurationValidationReport(List.of());
    }

    public static ConfigurationValidationReport of(List<ValidationIssue> issues) {
        return new ConfigurationValidationReport(issues);
    }

    public static ConfigurationValidationReport of(ValidationIssue issue) {
        return of(List.of(issue));
    }

    public static ConfigurationValidationReport ofError(String message) {
        return init().error(message);
    }

    public ConfigurationValidationReport error(String message) {
        this.issues.add(ValidationIssue.error(null, message));
        return this;
    }

    public ConfigurationValidationReport warning(String message) {
        this.issues.add(ValidationIssue.warning(null, message));
        return this;
    }

    public ConfigurationValidationReport missingProperty(String property) {
        return error("Property '" + property + "' must be set");
    }

    public boolean isValid() {
        return issues.stream().anyMatch(i -> i.getType() != ValidationIssue.Type.None);
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
