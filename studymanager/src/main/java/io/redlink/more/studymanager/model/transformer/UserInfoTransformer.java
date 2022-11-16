/*
 * Copyright (c) 2022 Redlink GmbH.
 */
package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.PlatformPermissionDTO;
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
                .permissions(toPlatformPermissions(user.roles()));
    }

    public static Set<PlatformPermissionDTO> toPlatformPermissions(Set<MoreUser.Role> roles) {
        return roles.stream()
                .map(UserInfoTransformer::toPlatformPermission)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    public static PlatformPermissionDTO toPlatformPermission(MoreUser.Role role) {
        return switch (role) {
            case STUDY_VIEWER -> PlatformPermissionDTO.READ_STUDIES;
            case STUDY_CREATOR -> PlatformPermissionDTO.CREATE_STUDIES;
            default -> null;
        };
    }

}
