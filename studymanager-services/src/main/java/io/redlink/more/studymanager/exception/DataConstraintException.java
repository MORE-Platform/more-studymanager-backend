/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.exception;

import io.redlink.more.studymanager.model.StudyRole;
import org.apache.commons.lang3.StringUtils;
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

    public static DataConstraintException createWithMessage(long studyId, String item, String reason) {
        var msg = "Unable to remove %s from study_%d".formatted(item, studyId);
        return new DataConstraintException(
                StringUtils.isNotBlank(reason) ? "%s (reason: %s)".formatted(msg, reason) : msg);
    }

    public static DataConstraintException createMilestoneInUseByActiveParticipant(long studyId, int milestoneId) {
        return new DataConstraintException(
                "Can't delete milestone_%d from study_%d: An active participant has this milestone set!"
                        .formatted(milestoneId, studyId)
        );
    }

    public static DataConstraintException createParticipantMilestoneAlreadyExists(long studyId, int participantId, int milestoneId) {
        return new DataConstraintException(
                "A participant milestone for milestone_%d already exists for participant_%d in study_%d"
                        .formatted(milestoneId, participantId, studyId)
        );
    }

    public static DataConstraintException createMilestoneInUseByObservation(long studyId, int milestoneId) {
        return new DataConstraintException(
                "Can't delete milestone_%d from study_%d: An observation still references this milestone!"
                        .formatted(milestoneId, studyId)
        );
    }

    public static DataConstraintException createMilestoneInUseByIntervention(long studyId, int milestoneId) {
        return new DataConstraintException(
                "Can't delete milestone_%d from study_%d: An intervention still references this milestone!"
                        .formatted(milestoneId, studyId)
        );
    }

}
