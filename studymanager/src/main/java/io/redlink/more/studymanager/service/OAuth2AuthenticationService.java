/*
 * Copyright (c) 2022 Redlink GmbH.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.model.AttributeMapClaimAccessor;
import io.redlink.more.studymanager.model.AuthenticatedUser;
import io.redlink.more.studymanager.model.PlatformRole;
import io.redlink.more.studymanager.properties.MoreAuthProperties;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimAccessor;
import org.springframework.security.oauth2.core.oidc.StandardClaimAccessor;

public class OAuth2AuthenticationService {

    private final Map<String, Set<PlatformRole>> roleMapping;
    private final MoreAuthProperties.ClaimsProperties claimSettings;

    public OAuth2AuthenticationService(MoreAuthProperties moreAuthProperties) {
        claimSettings = Objects.requireNonNull(moreAuthProperties.claims(), "claims must not be null");
        Objects.requireNonNull(moreAuthProperties.globalRoles(), "globalRoles must not be null");

        var mapping = new HashMap<String, EnumSet<PlatformRole>>();
        moreAuthProperties.globalRoles().forEach(
                (moreRole, authRoles) ->
                        authRoles.forEach(
                                authRole -> mapping
                                        .computeIfAbsent(authRole, k -> EnumSet.noneOf(PlatformRole.class))
                                        .add(moreRole)
                        )
        );
        mapping.replaceAll((key, value) -> EnumSet.copyOf(value));
        this.roleMapping = Map.copyOf(mapping);
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public IdTokenClaimAccessor getClaimAccessor() {
        return getStandardClaimAccessor(getAuthentication());
    }

    public static IdTokenClaimAccessor getStandardClaimAccessor(Authentication authentication) {
        return Optional.ofNullable(authentication)
                .map(Authentication::getPrincipal)
                .filter(ClaimAccessor.class::isInstance)
                .map(ClaimAccessor.class::cast)
                .map(AttributeMapClaimAccessor::new)
                .orElse(null);
    }

    public AuthenticatedUser getCurrentUser() {
        return getAuthenticatedUser(getClaimAccessor());
    }

    public AuthenticatedUser getAuthenticatedUser(Authentication authentication) {
        return getAuthenticatedUser(getStandardClaimAccessor(authentication));
    }

    public AuthenticatedUser getAuthenticatedUser(ClaimAccessor ca) {
        if (ca != null) {
            final var claims = new AttributeMapClaimAccessor(ca);
            return new AuthenticatedUser(
                    claims.getSubject(),
                    claims.getFullName(),
                    Boolean.TRUE.equals(claims.getEmailVerified()) ? claims.getEmail() : null,
                    claims.getClaimAsString(claimSettings.institution()),
                    extractRoles(claims)
            );
        }

        return new AuthenticatedUser(
                null,
                null,
                null,
                null,
                EnumSet.noneOf(PlatformRole.class)
        );
    }

    public Set<PlatformRole> extractRoles(Map<String, Object> attributes) {
        return extractRoles(new AttributeMapClaimAccessor(attributes));
    }

    public Set<PlatformRole> extractRoles(ClaimAccessor token) {
        return mapToRoles(token.getClaimAsStringList(claimSettings.roles()));
    }

    private Set<PlatformRole> mapToRoles(List<String> roles) {
        return roles.stream()
                .map(roleMapping::get)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean validateProfile(StandardClaimAccessor userInfo) {
        return getAuthenticatedUser(userInfo).isValid();
    }


}
