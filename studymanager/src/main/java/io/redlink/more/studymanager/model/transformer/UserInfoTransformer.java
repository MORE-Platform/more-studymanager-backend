/*
 * Copyright (c) 2022 Redlink GmbH.
 */
package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.PlatformRoleDTO;
import io.redlink.more.studymanager.api.v1.model.UserInfoDTO;
import io.redlink.more.studymanager.model.MoreUser;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class UserInfoTransformer {

    private UserInfoTransformer() {}

    public static UserInfoDTO toUserInfoDTO(MoreUser user) {
        return new UserInfoDTO()
                .name(user.fullName())
                .email(user.email())
                .institution(user.institution())
                .roles(toPlatformRoles(user.roles()));
    }

    public static Set<PlatformRoleDTO> toPlatformRoles(Set<MoreUser.Role> roles) {
        return roles.stream()
                .map(UserInfoTransformer::toPlatformRole)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    public static PlatformRoleDTO toPlatformRole(MoreUser.Role role) {
        return switch (role) {
            case STUDY_VIEWER -> PlatformRoleDTO.VIEWER;
            case STUDY_CREATOR -> PlatformRoleDTO.OPERATOR;
            default -> null;
        };
    }

}
