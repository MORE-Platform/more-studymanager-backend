/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.PlatformRoleDTO;
import io.redlink.more.studymanager.api.v1.model.StudyRoleDTO;
import io.redlink.more.studymanager.model.PlatformRole;
import io.redlink.more.studymanager.model.StudyRole;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class RoleTransformer {

    private RoleTransformer() {
    }

    @SuppressWarnings("java:S1168")
    public static Set<StudyRoleDTO> toStudyRolesDTO(Collection<StudyRole> roles) {
        if (roles == null) {
            return null;
        }

        return roles.stream()
                .map(RoleTransformer::toStudyRoleDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet())
                ;
    }

    public static StudyRoleDTO toStudyRoleDTO(StudyRole studyRole) {
        return switch (studyRole) {
            case STUDY_VIEWER -> StudyRoleDTO.STUDY_VIEWER;
            case STUDY_OPERATOR -> StudyRoleDTO.STUDY_OPERATOR;
            case STUDY_ADMIN -> StudyRoleDTO.STUDY_ADMIN;
        };
    }

    public static Set<StudyRole> toStudyRoles(Collection<StudyRoleDTO> roles) {
        if (roles == null) {
            return Set.of();
        }

        return roles.stream()
                .map(RoleTransformer::toStudyRole)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet())
                ;
    }

    public static StudyRole toStudyRole(StudyRoleDTO studyRole) {
        return switch (studyRole) {
            case STUDY_VIEWER -> StudyRole.STUDY_VIEWER;
            case STUDY_OPERATOR -> StudyRole.STUDY_OPERATOR;
            case STUDY_ADMIN -> StudyRole.STUDY_ADMIN;
        };
    }

    @SuppressWarnings("java:S1168")
    public static Set<PlatformRoleDTO> toPlatformRolesDTO(Set<PlatformRole> roles) {
        if (roles == null) {
            return null;
        }

        return roles.stream()
                .map(RoleTransformer::toPlatformRoleDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet())
                ;
    }

    public static PlatformRoleDTO toPlatformRoleDTO(PlatformRole role) {
        return switch (role) {
            case MORE_VIEWER -> PlatformRoleDTO.MORE_VIEWER;
            case MORE_OPERATOR -> PlatformRoleDTO.MORE_OPERATOR;
            case MORE_ADMIN -> PlatformRoleDTO.MORE_ADMIN;
        };
    }

    public static Set<PlatformRole> toPlatformRoles(Set<PlatformRoleDTO> roles) {
        if (roles == null) {
            return Set.of();
        }

        return roles.stream()
                .map(RoleTransformer::toPlatformRole)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet())
                ;
    }

    public static PlatformRole toPlatformRole(PlatformRoleDTO role) {
        return switch (role) {
            case MORE_VIEWER -> PlatformRole.MORE_VIEWER;
            case MORE_OPERATOR -> PlatformRole.MORE_OPERATOR;
            case MORE_ADMIN -> PlatformRole.MORE_ADMIN;
        };
    }

}
