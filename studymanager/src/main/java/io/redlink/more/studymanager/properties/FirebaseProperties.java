package io.redlink.more.studymanager.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "firebase")
public record FirebaseProperties(
        Resource settingsFile
) {}
