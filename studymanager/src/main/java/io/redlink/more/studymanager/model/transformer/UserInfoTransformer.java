/*
 * Copyright (c) 2022 Redlink GmbH.
 */
package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.UserInfoDTO;
import io.redlink.more.studymanager.model.AuthenticatedUser;

public final class UserInfoTransformer {

    private UserInfoTransformer() {}

    public static UserInfoDTO toUserInfoDTO(AuthenticatedUser user) {
        return new UserInfoDTO()
                .name(user.fullName())
                .email(user.email())
                .institution(user.institution())
                .roles(RoleTransformer.toPlatformRolesDTO(user.roles()));
    }

}
