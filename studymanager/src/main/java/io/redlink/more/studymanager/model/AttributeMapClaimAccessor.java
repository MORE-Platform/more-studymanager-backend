/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
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
