package io.redlink.more.studymanager.model;

import java.util.Set;

public record AuthenticatedUser(
        String id,
        String fullName,
        String email,
        String institution,
        Set<PlatformRole> roles
) {

    boolean canViewStudies() {
        return roles.contains(PlatformRole.MORE_VIEWER);
    }

    boolean canCreateStudies() {
        return roles.contains(PlatformRole.MORE_OPERATOR);
    }

}
