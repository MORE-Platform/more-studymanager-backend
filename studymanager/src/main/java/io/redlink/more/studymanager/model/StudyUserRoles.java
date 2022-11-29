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
