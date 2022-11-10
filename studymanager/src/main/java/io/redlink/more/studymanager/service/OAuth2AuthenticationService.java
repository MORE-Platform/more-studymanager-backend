/*
 * Copyright (c) 2022 Redlink GmbH.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.model.MoreUser;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.StandardClaimAccessor;

public class OAuth2AuthenticationService {

    private static final Map<String, MoreUser.Roles> ROLE_MAPPING = Map.of(
            "study-access", MoreUser.Roles.STUDY_VIEWER,
            "study-creator", MoreUser.Roles.STUDY_CREATOR
    );

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private StandardClaimAccessor getClaimAccessor() {
        if (getAuthentication() instanceof StandardClaimAccessor sca) {
            return sca;
        }
        return null;
    }

    public MoreUser getCurrentUser() {
        final StandardClaimAccessor claims = getClaimAccessor();
        if (claims != null)
            return new MoreUser(
                    claims.getSubject(),
                    claims.getGivenName(),
                    claims.getFamilyName(),
                    claims.getFullName(),
                    Boolean.TRUE.equals(claims.getEmailVerified()) ? claims.getEmail() : null,
                    claims.getClaimAsString("org"),
                    mapToRoles(claims.getClaimAsStringList("roles"))
            );

        return new MoreUser(
                null,
                null,
                null,
                null,
                null,
                null,
                EnumSet.noneOf(MoreUser.Roles.class)
        );
    }

    private static Set<MoreUser.Roles> mapToRoles(List<String> roles) {
        return roles.stream()
                .map(ROLE_MAPPING::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

}
