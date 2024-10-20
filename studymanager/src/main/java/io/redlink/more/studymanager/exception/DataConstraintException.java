/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.exception;

import io.redlink.more.studymanager.model.StudyRole;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DataConstraintException extends RuntimeException {

    public DataConstraintException(String message) {
        super(message);
    }

    public static DataConstraintException createOneStudyAdminRequired(long studyId, String userId) {
        return new DataConstraintException(
                "Can't remove %s from study_%d: At least one %s is required!"
                        .formatted(userId, studyId, StudyRole.STUDY_ADMIN)
        );
    }

    public static DataConstraintException createNoSelfAdminRemoval(long studyId, String userId) {
        return new DataConstraintException(
                "Removing yourself (%s) as %s from study_%d is not allowed."
                        .formatted(userId, StudyRole.STUDY_ADMIN, studyId)
        );
    }
}
