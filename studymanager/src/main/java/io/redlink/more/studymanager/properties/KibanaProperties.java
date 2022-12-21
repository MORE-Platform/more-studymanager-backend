package io.redlink.more.studymanager.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "kibana")
public record KibanaProperties(
        URI uri
) { }
