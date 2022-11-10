package io.redlink.more.studymanager.model;

import java.util.Set;

public record MoreUser(
        String id,
        String fistName,
        String lastName,
        String fullName,
        String email,
        String institution,
        Set<Roles> roles
) {

    public enum Roles {
        STUDY_VIEWER,
        STUDY_CREATOR
    }

    boolean canViewStudies() {
        return roles.contains(Roles.STUDY_VIEWER);
    }

    boolean canCreateStudies() {
        return roles.contains(Roles.STUDY_CREATOR);
    }

}
