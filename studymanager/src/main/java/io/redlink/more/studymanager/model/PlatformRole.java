/*
 * Copyright (c) 2022 Redlink GmbH.
 */
package io.redlink.more.studymanager.model;

import java.util.Locale;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Roles a user can have on the global/platform level
 */
public enum PlatformRole {
    /**
     * Can <em>view existing</em> studies (where listed in the ACL)
     */
    MORE_VIEWER,
    /**
     * Can <em>create new</em> studies
     */
    MORE_OPERATOR,
    /**
     * Can <em>manage collaborators</em> on all studies
     */
    MORE_ADMIN,

    ;

    private final GrantedAuthority authority;

    PlatformRole() {
        authority = new SimpleGrantedAuthority("ROLE_%s".formatted(name()).toUpperCase(Locale.ROOT));
    }

    public GrantedAuthority authority() {
        return authority;
    }
}
