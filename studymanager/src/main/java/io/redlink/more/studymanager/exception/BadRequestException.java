/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.exception;

import io.redlink.more.studymanager.model.Study;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    public BadRequestException(String cause) {
        super(cause);
    }

    public BadRequestException(String cause, Throwable throwable) {
        super(String.format("%s: %s", cause, throwable.getMessage()), throwable);
    }

    public BadRequestException(Throwable cause) {
        super(cause);
    }

    public static BadRequestException StateChange(Study.Status before, Study.Status after) {
        return new BadRequestException(String.format("StateChange not allowed: %s -> %s", before, after));
    }
}
