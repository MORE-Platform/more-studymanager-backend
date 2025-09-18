/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.core.exception;

import io.redlink.more.studymanager.core.validation.ConfigurationValidationReport;
import io.redlink.more.studymanager.core.validation.ValidationIssue;
import java.util.List;

public class ConfigurationValidationException extends RuntimeException {

    private final ConfigurationValidationReport report;

    public ConfigurationValidationException(ConfigurationValidationReport report) {
        this.report = report;
    }

    public ConfigurationValidationReport getReport() {
        return report;
    }

    @Override
    public String getMessage() {
        return report.toString();
    }

    public static ConfigurationValidationException of(ConfigurationValidationReport report) {
        return new ConfigurationValidationException(report);
    }

    public static ConfigurationValidationException of(ValidationIssue issue) {
        return of(ConfigurationValidationReport.of(issue));
    }

    public static ConfigurationValidationException of(List<ValidationIssue> issues) {
        return of(ConfigurationValidationReport.of(issues));
    }

    public static ConfigurationValidationException ofError(String message) {
        return of(ConfigurationValidationReport.ofError(message));
    }
}
