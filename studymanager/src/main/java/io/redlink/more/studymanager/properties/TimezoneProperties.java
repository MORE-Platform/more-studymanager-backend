package io.redlink.more.studymanager.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "more.timezone")
public record TimezoneProperties (
        String identifier
) {}
