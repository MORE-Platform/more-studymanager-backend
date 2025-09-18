/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.CurrentUserDTO;
import io.redlink.more.studymanager.api.v1.model.UserSearchResultListDTO;
import io.redlink.more.studymanager.api.v1.model.UserSearchResultListQueryDTO;
import io.redlink.more.studymanager.api.v1.webservices.UsersApi;
import io.redlink.more.studymanager.model.transformer.UserInfoTransformer;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import io.redlink.more.studymanager.service.UserService;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimAccessor;
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
        final IdTokenClaimAccessor claimAccessor = authService.getClaimAccessor();

        final URL issuer = claimAccessor.getIssuer();
        final URI profileUrl = buildProfileUrl(issuer);
        final URI userManagementUrl = buildUserManagementUrl(issuer);

        return ResponseEntity.ok(
                UserInfoTransformer.toCurrentUserDTO(
                        authService.getAuthenticatedUser(claimAccessor),
                        profileUrl,
                        userManagementUrl
                )
        );
    }

    private URI buildProfileUrl(URL issuerUrl) {
        try {
            if (issuerUrl != null) {
                var b = new URIBuilder(issuerUrl.toURI());
                var ps = new ArrayList<>(b.getPathSegments());
                ps.add("account");
                b.setPathSegments(ps);
                return b.build();
            }
        } catch (URISyntaxException e) {
            // empty
        }
        return null;
    }

    private URI buildUserManagementUrl(URL issuerUrl) {
        try {
            if (issuerUrl != null) {
                var b = new URIBuilder(issuerUrl.toURI());
                b.setPath(b.getPath().replace("/realms/", "/admin/") + "/console");
                return b.build();
            }
        } catch (URISyntaxException e) {
            // empty
        }
        return null;
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
