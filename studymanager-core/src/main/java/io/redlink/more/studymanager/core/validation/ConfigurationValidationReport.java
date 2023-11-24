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
        this.issues = issues;
    }

    public static ConfigurationValidationReport init() {
        return new ConfigurationValidationReport(new ArrayList<>());
    }

    public static ConfigurationValidationReport of(List<ValidationIssue> issues) {
        //TODO copy
        return new ConfigurationValidationReport(issues);
    }

    public static ConfigurationValidationReport of(ValidationIssue issue) {
        return new ConfigurationValidationReport(List.of(issue));
    }

    public ConfigurationValidationReport error(String message) {
        this.issues.add(ValidationIssue.error(null,message));
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
