package io.redlink.more.studymanager.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "firebase")
@ConstructorBinding
public record FirebaseProperties(
        Resource settingsFile
) {}
