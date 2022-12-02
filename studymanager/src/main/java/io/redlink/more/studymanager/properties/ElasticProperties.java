package io.redlink.more.studymanager.properties;

import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "elastic")
public record ElasticProperties(
        URI uri,
        String host,
        int port,
        String username,
        String password,
        String numberOfShards
) { }
