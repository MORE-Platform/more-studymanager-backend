package io.redlink.more.studymanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {
    public ValidationException(String cause) {
        super(cause);
    }
}
