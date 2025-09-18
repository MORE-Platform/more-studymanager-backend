/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.ValidationReportDTO;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.model.transformer.ValidationReportTransformer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(basePackageClasses = ExceptionControllerAdvice.class)
public class ExceptionControllerAdvice {

    @ExceptionHandler(ConfigurationValidationException.class)
    public ResponseEntity<ValidationReportDTO> handleConfigurationValidationException(ConfigurationValidationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .header("Error-Message", e.getMessage())
                .body(ValidationReportTransformer.validationReportDTO_V1(e));
    }

}
