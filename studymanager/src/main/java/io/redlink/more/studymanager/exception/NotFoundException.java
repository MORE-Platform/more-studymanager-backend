/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {

    public NotFoundException(String type) {
        super(String.format("%s cannot be found", type));
    }

    public NotFoundException(String type, Object id) {
        super(String.format("%s with id %s cannot be found", type, id.toString()));
    }
    public static NotFoundException Study(long id) {
        return new NotFoundException("Study", id);
    }

    public static NotFoundException StudyGroup(long studyId, int studyGroupId) {
        return new NotFoundException("StudyGroup", studyId + "/" + studyGroupId);
    }

    public static NotFoundException ObservationFactory(String type) {
        return new NotFoundException("Observation Factory '" + type + "'");
    }

    public static NotFoundException ActionFactory(String type) {
        return new NotFoundException("Action Factory '" + type + "'");
    }

    public static NotFoundException TriggerFactory(String type) {
        return new NotFoundException("Trigger Factory '" + type + "'");
    }
}
