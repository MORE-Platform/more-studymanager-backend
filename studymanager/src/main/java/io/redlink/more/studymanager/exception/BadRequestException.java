package io.redlink.more.studymanager.exception;

import io.redlink.more.studymanager.model.Study;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    public BadRequestException(String cause) {
        super(cause);
    }

    public BadRequestException(Throwable cause) {
        super(cause);
    }

    public static BadRequestException StateChange(Study.Status before, Study.Status after) {
        return new BadRequestException(String.format("StateChange not allowed: %s -> %s", before, after));
    }
}
