/*
 * Copyright (c) 2022 Redlink GmbH.
 */
package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.StudyRoleDTO;
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

    private static StudyRoleDTO toStudyRoleDTO(StudyRole studyRole) {
        return switch (studyRole) {
            case STUDY_VIEWER -> StudyRoleDTO.VIEWER;
            case STUDY_OPERATOR -> StudyRoleDTO.OPERATOR;
            case STUDY_ADMIN -> StudyRoleDTO.ADMIN;
        };
    }

}
