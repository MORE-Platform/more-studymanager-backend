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
