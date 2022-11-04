package io.redlink.more.studymanager.core.exception;

import io.redlink.more.studymanager.core.validation.ConfigurationValidationReport;

public class ConfigurationValidationException extends Exception {
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
}
