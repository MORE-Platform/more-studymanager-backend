package io.redlink.more.studymanager.core.exception;

public class SchedulingException extends RuntimeException {
    public SchedulingException(Throwable cause) {
        super(cause);
    }

    public SchedulingException(String message) {
        super(message);
    }
}
