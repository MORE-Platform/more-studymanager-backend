package io.redlink.more.studymanager.exception;

import io.redlink.more.studymanager.model.Study;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT)
public class BadStudyStateException extends RuntimeException{

    public BadStudyStateException(String cause){ super(cause); }

    public BadStudyStateException(Throwable cause){ super(cause); }

    public static BadStudyStateException state(Study.Status state){
        return new BadStudyStateException(String.format("Cannot perform operation: Study in %s state", state));
    }
    public static BadStudyStateException state(){
        return new BadStudyStateException(("Bad study state for operation"));
    }
}