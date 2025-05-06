/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.properties;

import io.redlink.more.studymanager.model.Participant;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@ConfigurationProperties(prefix = "more.gateway")
public record GatewayProperties(String baseUrl) {
    public URI generateSignupUrl(Participant participant) {
        if (participant.getRegistrationToken() == null) return null;

        return UriComponentsBuilder.fromUriString(baseUrl)
                .path("signup")
                .queryParam("token", participant.getRegistrationToken())
                .build()
                .toUri();
    }
}
