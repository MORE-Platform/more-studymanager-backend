/*
 * Copyright (c) 2022 Redlink GmbH.
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

}
