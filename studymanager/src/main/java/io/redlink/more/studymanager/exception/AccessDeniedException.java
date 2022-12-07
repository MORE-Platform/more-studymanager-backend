/*
 * Copyright (c) 2022 Redlink GmbH.
 */
package io.redlink.more.studymanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException(String format, Object... args) {
        this(format.formatted(args));
    }
}
