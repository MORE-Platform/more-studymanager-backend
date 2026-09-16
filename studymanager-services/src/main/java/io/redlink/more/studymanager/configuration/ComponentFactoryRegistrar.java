/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License 2.0.
 */
package io.redlink.more.studymanager.configuration;

import io.redlink.more.studymanager.core.factory.ActionFactory;
import io.redlink.more.studymanager.core.factory.ComponentFactory;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.factory.TriggerFactory;
import io.redlink.more.studymanager.properties.ComponentFactoriesProperties;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * Registers all {@link ComponentFactory} implementations found on the classpath as bean definitions
 * named by their {@link ComponentFactory#getId() id}.
 * <p>
 * The registration has to happen while the bean definitions are collected - and not from a
 * {@code @PostConstruct} using {@code registerSingleton(..)}. Manually registered singletons only
 * become visible to type based injection once the registering bean has been initialised, so every
 * bean that was created before that (e.g. a controller injecting a factory or a
 * {@code Map<String, ObservationFactory>}) either got an empty map or failed to start - depending
 * on the - undefined - initialisation order.
 */
public class ComponentFactoryRegistrar implements BeanDefinitionRegistryPostProcessor, BeanFactoryAware {

    private final Logger logger = LoggerFactory.getLogger(ComponentFactoryRegistrar.class);

    private final Reflections reflections;
    private BeanFactory beanFactory;

    public ComponentFactoryRegistrar(String basePackage) {
        this.reflections = new Reflections(basePackage);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        register(registry, ObservationFactory.class);
        register(registry, TriggerFactory.class);
        register(registry, ActionFactory.class);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        // nothing to do, all factories are registered as bean definitions
    }

    private void register(BeanDefinitionRegistry registry, Class<? extends ComponentFactory> type) {
        reflections.getSubTypesOf(type).stream()
                .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                .forEach(c -> {
                    final ComponentFactory factory = instantiate(c);
                    final String id = factory.getId();
                    logger.trace("Registering {} factory: {}[class:{}]", type.getSimpleName(), id, c.getName());

                    final GenericBeanDefinition definition = new GenericBeanDefinition();
                    definition.setBeanClass(c);
                    definition.setInstanceSupplier(() -> factory.init(
                            beanFactory.getBean(ComponentFactoriesProperties.class).get(id)));
                    registry.registerBeanDefinition(id, definition);
                });
    }

    private ComponentFactory instantiate(Class<? extends ComponentFactory> c) {
        try {
            return c.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new IllegalStateException("Cannot instantiate component factory " + c.getName(), e);
        }
    }
}
