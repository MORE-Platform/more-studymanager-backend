/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.ValidationReportDTO;
import io.redlink.more.studymanager.api.v1.model.ValidationReportItemDTO;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.validation.ConfigurationValidationReport;
import io.redlink.more.studymanager.core.validation.ValidationIssue;
import java.util.List;

public final class ValidationReportTransformer {

    private ValidationReportTransformer() {
    }

    public static ValidationReportDTO validationReportDTO_V1(ConfigurationValidationException validation) {
        return validationReportDTO_V1(validation.getReport())
                .valid(false);
    }

    public static ValidationReportDTO validationReportDTO_V1(ConfigurationValidationReport report) {
        return new ValidationReportDTO()
                .valid(report.isValid())
                .errors(validationReportItemDTO_V1(report.getErrors(), "error"))
                .warnings(validationReportItemDTO_V1(report.getWarnings(), "warning"));
    }

    private static List<ValidationReportItemDTO> validationReportItemDTO_V1(List<ValidationIssue> issues, String type) {
        return issues.stream()
                .map(i -> validationReportItemDTO_V1(i, type))
                .toList();
    }

    private static ValidationReportItemDTO validationReportItemDTO_V1(ValidationIssue issue, String type) {
        return new ValidationReportItemDTO()
                .message(issue.getMessage())
                .propertyId(issue.getPropertyId())
                .componentTitle(issue.getComponentTitle())
                .type(type);
    }

}
