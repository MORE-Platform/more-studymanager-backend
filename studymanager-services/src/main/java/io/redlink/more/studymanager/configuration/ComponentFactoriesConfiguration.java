/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.configuration;

import io.redlink.more.studymanager.properties.ComponentFactoriesProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ComponentFactoriesProperties.class})
public class ComponentFactoriesConfiguration {

    static final String COMPONENT_PACKAGE = "io.redlink.more.studymanager.component";

    /**
     * Registered as {@code static} so that the registrar can do its work before any other bean
     * (controllers, services, ...) is instantiated.
     */
    @Bean
    public static ComponentFactoryRegistrar componentFactoryRegistrar() {
        return new ComponentFactoryRegistrar(COMPONENT_PACKAGE);
    }
}
