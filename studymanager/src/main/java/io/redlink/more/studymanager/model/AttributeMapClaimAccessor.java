/*
 * Copyright (c) 2022 Redlink GmbH.
 */
package io.redlink.more.studymanager.model;

import java.util.Map;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimAccessor;

public record AttributeMapClaimAccessor(Map<String, Object> claims) implements IdTokenClaimAccessor {

    public AttributeMapClaimAccessor {
        claims = Map.copyOf(claims);
    }

    public AttributeMapClaimAccessor(ClaimAccessor delegate) {
        this(delegate.getClaims());
    }

    @Override
    public Map<String, Object> getClaims() {
        return claims();
    }
}
