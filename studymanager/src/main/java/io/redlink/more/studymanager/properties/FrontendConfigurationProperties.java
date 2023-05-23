package io.redlink.more.studymanager.properties;

import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "more.frontend")
public record FrontendConfigurationProperties(
        String title,
        KeycloakProperties keycloak
) {

    public record KeycloakProperties(
            URI server,
            String realm,
            String clientId
    ) {}

}
