/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "more.gateway")
public class GatewayProperties {
    private String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }

    public GatewayProperties setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }
}
