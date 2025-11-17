/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model;

import java.time.Instant;
import java.util.Set;

public record StudyUserRoles(
        MoreUser user,
        Set<StudyRoleDetails> roles
) {


    public record StudyRoleDetails(
            StudyRole role,
            MoreUser creator,
            Instant created
    ) {

        public StudyRoleDetails(StudyRole role, Instant created) {
            this(role, null, created);
        }

    }

}
