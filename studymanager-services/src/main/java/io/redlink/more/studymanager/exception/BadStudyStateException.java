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

@ResponseStatus(code = HttpStatus.CONFLICT)
public class BadStudyStateException extends RuntimeException{

    public BadStudyStateException(String message){ super(message); }

    public BadStudyStateException(Throwable cause){ super(cause); }

    public static BadStudyStateException state(Study.Status state){
        return new BadStudyStateException(String.format("Cannot perform operation: Study in %s state", state));
    }
    public static BadStudyStateException state(){
        return new BadStudyStateException(("Bad study state for operation"));
    }
}