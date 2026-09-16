/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.configuration;

import io.redlink.more.studymanager.component.observation.lime.LimeSurveyObservationFactory;
import io.redlink.more.studymanager.core.factory.ActionFactory;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.factory.TriggerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        args = {
        "--more.components.lime-survey-observation.url=https://testurl",
                "--more.components.lime-survey-observation.username=testUsername",
                "--more.components.lime-survey-observation.password=testPassword"})
@ContextConfiguration(classes = {ComponentFactoriesConfiguration.class, ComponentConfigurationTest.FactoryConsumerConfig.class})
public class ComponentConfigurationTest {

    @Autowired
    ApplicationContext context;

    @Test
    public void testConfig() {
        LimeSurveyObservationFactory factory = context.getBean(LimeSurveyObservationFactory.class);
        Assertions.assertEquals(factory.componentProperties.get("username"), "testUsername");
        Assertions.assertEquals(factory.componentProperties.get("password"), "testPassword");
        Assertions.assertEquals(factory.componentProperties.get("url"), "https://testurl");
    }

    /**
     * The services look up component factories by their id (see e.g.
     * {@code InterventionService.factory(Trigger)}), so every factory must be registered
     * as a singleton under its id - not only the observation and action factories.
     */
    @Test
    public void testFactoriesAreRegisteredById() {
        Assertions.assertNotNull(context.getBean("relative-time-trigger", TriggerFactory.class));
        Assertions.assertNotNull(context.getBean("scheduled-trigger", TriggerFactory.class));
        Assertions.assertNotNull(context.getBean("scheduled-datacheck-trigger", TriggerFactory.class));
        Assertions.assertNotNull(context.getBean("lime-survey-observation", ObservationFactory.class));
        Assertions.assertNotNull(context.getBean("push-notification-action", ActionFactory.class));
    }

    /**
     * The factories must be injectable - by type as well as as a {@code Map<String, ...>} keyed by
     * their id - independent of the order in which the beans are initialised. See
     * {@link ComponentFactoryRegistrar}.
     */
    @Test
    public void testFactoriesAreInjectable() {
        FactoryConsumer consumer = context.getBean(FactoryConsumer.class);

        Assertions.assertNotNull(consumer.limeSurveyObservationFactory());
        Assertions.assertTrue(consumer.observationFactories().containsKey("lime-survey-observation"));
        Assertions.assertTrue(consumer.triggerFactories().containsKey("relative-time-trigger"));
        Assertions.assertTrue(consumer.actionFactories().containsKey("push-notification-action"));
    }

    /**
     * Factories registered as manual singletons are only visible to type based injection after the
     * registering bean has been initialised, which made the injection depend on the - undefined -
     * bean initialisation order. They therefore have to be registered as bean definitions.
     */
    @Test
    public void testFactoriesAreRegisteredAsBeanDefinitions() {
        var beanFactory = ((ConfigurableApplicationContext) context).getBeanFactory();

        Assertions.assertTrue(beanFactory.containsBeanDefinition("lime-survey-observation"));
        Assertions.assertTrue(beanFactory.containsBeanDefinition("relative-time-trigger"));
        Assertions.assertTrue(beanFactory.containsBeanDefinition("push-notification-action"));
    }

    @TestConfiguration
    static class FactoryConsumerConfig {
        @Bean
        public FactoryConsumer factoryConsumer(
                LimeSurveyObservationFactory limeSurveyObservationFactory,
                Map<String, ObservationFactory> observationFactories,
                Map<String, TriggerFactory> triggerFactories,
                Map<String, ActionFactory> actionFactories) {
            return new FactoryConsumer(limeSurveyObservationFactory, observationFactories, triggerFactories, actionFactories);
        }
    }

    record FactoryConsumer(
            LimeSurveyObservationFactory limeSurveyObservationFactory,
            Map<String, ObservationFactory> observationFactories,
            Map<String, TriggerFactory> triggerFactories,
            Map<String, ActionFactory> actionFactories) {
    }
}
