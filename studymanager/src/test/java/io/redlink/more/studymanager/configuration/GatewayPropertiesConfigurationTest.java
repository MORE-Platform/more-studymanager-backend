package io.redlink.more.studymanager.configuration;

import io.redlink.more.studymanager.properties.GatewayProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class GatewayPropertiesConfigurationTest {
    public static final String SIGNUP_URL = "https://example.com";

    @Bean
    GatewayProperties gatewayProperties() {
        return new GatewayProperties(SIGNUP_URL);
    }
}
