/*
 * Copyright (c) 2022 Redlink GmbH.
 */
package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.PlatformRoleDTO;
import io.redlink.more.studymanager.api.v1.model.UserInfoDTO;
import io.redlink.more.studymanager.model.AuthenticatedUser;
import io.redlink.more.studymanager.model.PlatformRole;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class UserInfoTransformer {

    private UserInfoTransformer() {}

    public static UserInfoDTO toUserInfoDTO(AuthenticatedUser user) {
        return new UserInfoDTO()
                .name(user.fullName())
                .email(user.email())
                .institution(user.institution())
                .roles(toPlatformRoles(user.roles()));
    }

    public static Set<PlatformRoleDTO> toPlatformRoles(Set<PlatformRole> roles) {
        return roles.stream()
                .map(UserInfoTransformer::toPlatformRole)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    public static PlatformRoleDTO toPlatformRole(PlatformRole role) {
        return switch (role) {
            case MORE_VIEWER -> PlatformRoleDTO.VIEWER;
            case MORE_OPERATOR -> PlatformRoleDTO.OPERATOR;
            default -> null;
        };
    }

}
