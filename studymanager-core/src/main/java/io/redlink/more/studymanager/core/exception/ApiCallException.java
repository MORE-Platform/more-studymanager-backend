package io.redlink.more.studymanager.core.exception;
import org.apache.http.client.HttpResponseException;

public class ApiCallException extends HttpResponseException {
    public ApiCallException(int status, String message) {
        super(status, message);
    }
}
