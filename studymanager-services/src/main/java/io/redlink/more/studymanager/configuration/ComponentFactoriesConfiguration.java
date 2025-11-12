/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.configuration;

import io.redlink.more.studymanager.core.factory.ActionFactory;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.factory.TriggerFactory;
import io.redlink.more.studymanager.properties.ComponentFactoriesProperties;
import org.reflections.Reflections;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties({ComponentFactoriesProperties.class})
public class ComponentFactoriesConfiguration implements BeanFactoryAware {
    private BeanFactory beanFactory;

    private final Reflections reflections;
    private final ComponentFactoriesProperties componentFactoriesProperties;

    public ComponentFactoriesConfiguration(ComponentFactoriesProperties componentFactoriesProperties) {
        this.reflections = new Reflections("io.redlink.more.studymanager.component");
        this.componentFactoriesProperties = componentFactoriesProperties;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Bean
    public Map<String, TriggerFactory> triggerFactoryMap() {
        Set<Class<? extends TriggerFactory>> triggerFactories = reflections.getSubTypesOf(TriggerFactory.class);
        return triggerFactories.stream().map(this::instantiate)
                .collect(Collectors.toMap(
                (trigger) -> trigger.getId(),
                (trigger) -> trigger
        ));
    }

    @PostConstruct
    public void onPostConstruct() {
        ConfigurableBeanFactory configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;

        Set<Class<? extends ObservationFactory>> observationFactories = reflections.getSubTypesOf(ObservationFactory.class);
        observationFactories.stream().map(this::instantiate)
                .map(f -> f.init(componentFactoriesProperties.get(f.getId())))
                .forEach(m ->
                configurableBeanFactory.registerSingleton(m.getId(), m)
        );

        /*
        Set<Class<? extends TriggerFactory>> triggerFactories = reflections.getSubTypesOf(TriggerFactory.class);
        triggerFactories.stream().map(this::instantiate).forEach(m ->
                configurableBeanFactory.registerSingleton(m.getId(), m)
        );*/

        Set<Class<? extends ActionFactory>> actionFactories = reflections.getSubTypesOf(ActionFactory.class);
        actionFactories.stream().map(this::instantiate).forEach(m ->
                configurableBeanFactory.registerSingleton(m.getId(), m)
        );
    }

    private <T> T instantiate(Class<? extends T> c) {
        try {
            return c.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
