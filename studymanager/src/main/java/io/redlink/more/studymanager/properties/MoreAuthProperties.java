package io.redlink.more.studymanager.properties;

import io.redlink.more.studymanager.model.MoreUser;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "more.auth")
public record MoreAuthProperties(
        Map<MoreUser.Role, Set<String>> globalRoles
) {

    public MoreAuthProperties {
        globalRoles = Objects.requireNonNullElse(globalRoles, Map.of());
    }

}
