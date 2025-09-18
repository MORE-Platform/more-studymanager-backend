/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.exception.AccessDeniedException;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.repository.StudyAclRepository;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class StudyPermissionService {

    private final StudyAclRepository studyAclRepository;

    private final OAuth2AuthenticationService authenticationService;

    public StudyPermissionService(StudyAclRepository studyAclRepository,
                                  OAuth2AuthenticationService authenticationService) {
        this.studyAclRepository = studyAclRepository;
        this.authenticationService = authenticationService;
    }

    public long assertCurrentUserRole(final long studyId, StudyRole role) {
        return assertRole(studyId, currentUserId(), role);
    }

    public long assertRole(final long studyId, String userId, StudyRole role) {
        if (!hasRole(studyId, userId, role)) {
            throw new AccessDeniedException(
                    "Access to Study<%d> denied for <%s>: Role <%s> required",
                    studyId, userId, role
            );
        }
        return studyId;
    }

    public boolean currentUserHasRole(long studyId,  StudyRole role) {
        return hasRole(studyId, currentUserId(), role);
    }

    public boolean hasRole(long studyId, String userId, StudyRole role) {
        return hasAnyRole(studyId, userId, Set.of(role));
    }

    public long assertCurrentUserHasAnyRole(final long studyId) {
        return assertAnyRole(studyId, currentUserId());
    }

    public long assertAnyRole(final long studyId, String userId) {
        return assertAnyRole(studyId, userId, EnumSet.allOf(StudyRole.class));
    }

    public boolean currentUserHasAnyRole(long studyId) {
        return hasAnyRole(studyId, currentUserId());
    }

    public boolean hasAnyRole(long studyId, String userId) {
        return hasAnyRole(studyId, userId, EnumSet.allOf(StudyRole.class));
    }

    public long assertCurrentUserHasAnyRole(final long studyId,  StudyRole... roles) {
        return assertAnyRole(studyId, currentUserId(), roles);
    }

    public long assertAnyRole(final long studyId, String userId, StudyRole... roles) {
        return assertAnyRole(studyId, userId, Set.of(roles));
    }

    public boolean currentUserHasAnyRole(long studyId,  StudyRole... roles) {
        return hasAnyRole(studyId, currentUserId(), roles);
    }

    public boolean hasAnyRole(long studyId, String userId, StudyRole... roles) {
        return hasAnyRole(studyId, userId, Set.of(roles));
    }

    public long assertCurrentUserHasAnyRole(final long studyId,  Set<StudyRole> roles) {
        return assertAnyRole(studyId, currentUserId(), roles);
    }

    public long assertAnyRole(final long studyId, String userId, Set<StudyRole> roles) {
        if (!hasAnyRole(studyId, userId, roles)) {
            throw new AccessDeniedException(
                    "Access to Study<%d> denied for <%s>: Any role of %s required",
                    studyId, userId, roles
            );
        }
        return studyId;
    }

    public boolean currentUserHasAnyRole(long studyId,  Set<StudyRole> roles) {
        return hasAnyRole(studyId, currentUserId(), roles);
    }

    public boolean hasAnyRole(long studyId, String userId, Set<StudyRole> roles) {
        return studyAclRepository.hasAnyRole(studyId, userId, roles);
    }

    public long assertCurrentUserHasAllRoles(final long studyId) {
        return assertAllRoles(studyId, currentUserId());
    }

    public long assertAllRoles(final long studyId, String userId) {
        return assertAllRoles(studyId, userId, EnumSet.allOf(StudyRole.class));
    }

    public boolean currentUserHasAllRoles(long studyId) {
        return hasAllRoles(studyId, currentUserId());
    }

    public boolean hasAllRoles(long studyId, String userId) {
        return hasAllRoles(studyId, userId, EnumSet.allOf(StudyRole.class));
    }

    public long assertCurrentUserHasAllRoles(final long studyId,  StudyRole... roles) {
        return assertAllRoles(studyId, currentUserId(), roles);
    }

    public long assertAllRoles(final long studyId, String userId, StudyRole... roles) {
        return assertAllRoles(studyId, userId, Set.of(roles));
    }

    public boolean currentUserHasAllRoles(long studyId,  StudyRole... roles) {
        return hasAllRoles(studyId, currentUserId(), roles);
    }

    public boolean hasAllRoles(long studyId, String userId, StudyRole... roles) {
        return hasAllRoles(studyId, userId, Set.of(roles));
    }

    public long assertCurrentUserHasAllRoles(final long studyId,  Set<StudyRole> roles) {
        return assertAllRoles(studyId, currentUserId(), roles);
    }

    public long assertAllRoles(final long studyId, String userId, Set<StudyRole> roles) {
        if (!hasAllRoles(studyId, userId, roles)) {
            throw new AccessDeniedException(
                    "Access to Study<%d> denied for <%s>: All roles %s required",
                    studyId, userId, roles
            );
        }
        return studyId;
    }

    public boolean currentUserHasAllRoles(long studyId,  Set<StudyRole> roles) {
        return hasAllRoles(studyId, currentUserId(), roles);
    }

    public boolean hasAllRoles(long studyId, String userId, Set<StudyRole> roles) {
        return studyAclRepository.hasAllRoles(studyId, userId, roles);
    }


    private String currentUserId() {
        var currentUser = authenticationService.getCurrentUser();
        return currentUser != null ? currentUser.id() : null;
    }

}
