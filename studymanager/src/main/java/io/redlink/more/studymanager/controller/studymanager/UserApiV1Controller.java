/*
 * Copyright (c) 2022 Redlink GmbH.
 */
package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.CurrentUserDTO;
import io.redlink.more.studymanager.api.v1.model.UserSearchResultListDTO;
import io.redlink.more.studymanager.api.v1.model.UserSearchResultListQueryDTO;
import io.redlink.more.studymanager.api.v1.webservices.UsersApi;
import io.redlink.more.studymanager.model.transformer.UserInfoTransformer;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import io.redlink.more.studymanager.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserApiV1Controller implements UsersApi {

    private final OAuth2AuthenticationService authService;

    private final UserService userService;

    public UserApiV1Controller(OAuth2AuthenticationService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @Override
    public ResponseEntity<CurrentUserDTO> getCurrentUser() {
        return ResponseEntity.ok(
                UserInfoTransformer.toCurrentUserDTO(
                        authService.getCurrentUser()
                )
        );
    }

    @Override
    public ResponseEntity<UserSearchResultListDTO> findUsers(String q, Integer offset, Integer limit) {
        limit = Math.min(limit, 15);
        return ResponseEntity.ok(
                new UserSearchResultListDTO()
                        .query(new UserSearchResultListQueryDTO()
                                .q(q)
                                .offset(offset)
                                .limit(limit)
                        )
                        .result(
                                UserInfoTransformer.toUserSearchResultListDTO(
                                        userService.findUsers(q, offset, limit)
                                )
                        )
        );
    }
}
