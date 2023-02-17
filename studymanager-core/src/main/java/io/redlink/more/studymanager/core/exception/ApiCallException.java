package io.redlink.more.studymanager.core.exception;

public class ApiCallException extends Exception {
    private int status;
    public ApiCallException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
