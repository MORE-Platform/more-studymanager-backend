/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.CollaboratorDTO;
import io.redlink.more.studymanager.api.v1.model.CollaboratorDetailsDTO;
import io.redlink.more.studymanager.api.v1.model.CollaboratorRoleDetailsDTO;
import io.redlink.more.studymanager.api.v1.model.CurrentUserDTO;
import io.redlink.more.studymanager.api.v1.model.ProfileLinksDTO;
import io.redlink.more.studymanager.api.v1.model.UserInfoDTO;
import io.redlink.more.studymanager.api.v1.model.UserSearchResultListResultDTO;
import io.redlink.more.studymanager.model.AuthenticatedUser;
import io.redlink.more.studymanager.model.MoreUser;
import io.redlink.more.studymanager.model.SearchResult;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.StudyUserRoles;
import io.redlink.more.studymanager.model.User;
import java.net.URI;
import java.util.Set;
import java.util.stream.Collectors;

public final class UserInfoTransformer {

    private UserInfoTransformer() {}

    public static UserInfoDTO toUserInfoDTO(User user) {
        if (user == null) return null;

        return new UserInfoDTO()
                .uid(user.id())
                .name(user.fullName())
                .email(user.email())
                .institution(user.institution());
    }

    public static CurrentUserDTO toCurrentUserDTO(AuthenticatedUser user,
                                                  URI profileUrl, URI userManagementUrl) {
        if (user == null) return null;

        return new CurrentUserDTO()
                .uid(user.id())
                .name(user.fullName())
                .email(user.email())
                .institution(user.institution())
                .completeProfile(user.isValid())
                .roles(RoleTransformer.toPlatformRolesDTO(user.roles()))
                .links(
                        new ProfileLinksDTO()
                                .profile(profileUrl)
                                .userManagement(userManagementUrl)
                )
                ;
    }

    public static CollaboratorDTO toCollaboratorDTO(MoreUser user, Set<StudyRole> roles) {
        return new CollaboratorDTO()
                .user(toUserInfoDTO(user))
                .roles(RoleTransformer.toStudyRolesDTO(roles))
                ;
    }

    public static CollaboratorDetailsDTO toCollaboratorDetailsDTO(StudyUserRoles usr) {
        return new CollaboratorDetailsDTO()
                .user(toUserInfoDTO(usr.user()))
                .roles(toCollaboratorRoleDetailsDTO(usr.roles()));
    }

    private static Set<CollaboratorRoleDetailsDTO> toCollaboratorRoleDetailsDTO(Set<StudyUserRoles.StudyRoleDetails> roles) {
        return roles.stream()
                .map(UserInfoTransformer::toCollaboratorRoleDetailsDTO)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static CollaboratorRoleDetailsDTO toCollaboratorRoleDetailsDTO(StudyUserRoles.StudyRoleDetails role) {
        return new CollaboratorRoleDetailsDTO()
                .role(RoleTransformer.toStudyRoleDTO(role.role()))
                .assignedBy(toUserInfoDTO(role.creator()))
                .assignedAt(Transformers.toOffsetDateTime(role.created()))
                ;
    }

    public static UserSearchResultListResultDTO toUserSearchResultListDTO(SearchResult<? extends User> searchResult) {
        return new UserSearchResultListResultDTO()
                .numFound(searchResult.numFound())
                .start(searchResult.offset())
                .users(
                        searchResult.content().stream()
                                .map(UserInfoTransformer::toUserInfoDTO)
                                .toList()
                )
                ;
    }
}
