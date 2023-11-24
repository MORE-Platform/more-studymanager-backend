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
}
