/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model;

import java.time.Instant;

public record EndpointToken(
    Integer tokenId,
    String tokenLabel,
    Instant created,
    String token
) {


    public EndpointToken withToken(String newToken) {
        return new EndpointToken(
                tokenId,
                tokenLabel,
                created,
                newToken
        );
    }
}
