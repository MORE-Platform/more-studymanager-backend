/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model;

import io.redlink.more.studymanager.exception.NotAuthorizedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class RoutingInfoUserDetails implements UserDetails {

    private final RoutingInfo routingInfo;                    // e.g., userDataReference
    private final Collection<? extends GrantedAuthority> authorities;

    // You can add more fields as needed (email, language, etc.)

    public RoutingInfoUserDetails(RoutingInfo routingInfo,
                                  Collection<? extends GrantedAuthority> authorities) {

        this.routingInfo = Objects.requireNonNull(routingInfo, "routingInfo cannot be null");
        this.authorities = authorities != null ? Collections.unmodifiableCollection(authorities) : Collections.emptyList();
    }

    // ==================== UserDetails methods ====================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "unused";
    }

    @Override
    public String getUsername() {
        return String.format("study_%s-participant_%s", routingInfo.studyId(), routingInfo.participantId());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // ==================== Custom getters ====================

    public RoutingInfo getRoutingInfo() {
        return routingInfo;
    }

    // Optional: Helper method to get current user details easily
    public static RoutingInfoUserDetails getCurrent() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof RoutingInfoUserDetails details) {
            return details;
        }
        throw new NotAuthorizedException("This user is not authorized!");
    }
}
