package io.redlink.more.studymanager.properties;

import io.redlink.more.studymanager.core.factory.ComponentFactoryProperties;
import io.redlink.more.studymanager.core.properties.ComponentProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;

@ConfigurationProperties(prefix = "more.components")
public class ComponentFactoriesProperties extends HashMap<String, ComponentFactoryProperties> {
}
