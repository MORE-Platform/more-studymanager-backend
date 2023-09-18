package io.redlink.more.studymanager.properties;

import io.redlink.more.studymanager.model.PlatformRole;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "more.auth")
public record MoreAuthProperties(
        ClaimsProperties claims,
        Map<PlatformRole, Set<String>> globalRoles
) {

    public MoreAuthProperties {
        claims = Objects.requireNonNullElse(claims, new ClaimsProperties(null, null));
        globalRoles = Objects.requireNonNullElse(globalRoles, Map.of());
    }


    public record ClaimsProperties(
            String institution,
            String roles
    ) {
        public ClaimsProperties {
            institution = StringUtils.defaultIfEmpty(institution, "org");
            roles = StringUtils.defaultIfEmpty(roles, "realm_access.roles");
        }
    }

}
