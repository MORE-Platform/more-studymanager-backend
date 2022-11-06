package io.redlink.more.studymanager.configuration;

import io.redlink.more.studymanager.core.factory.ActionFactory;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.factory.TriggerFactory;
import org.reflections.Reflections;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

@Configuration
public class ComponentFactoriesConfiguration implements BeanFactoryAware {
    private BeanFactory beanFactory;

    private final Reflections reflections;

    public ComponentFactoriesConfiguration() {
        this.reflections = new Reflections("io.redlink.more.studymanager.component");
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @PostConstruct
    public void onPostConstruct() {
        ConfigurableBeanFactory configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;

        Set<Class<? extends ObservationFactory>> observationFactories = reflections.getSubTypesOf(ObservationFactory.class);
        observationFactories.stream().map(this::instantiate).forEach(m ->
                configurableBeanFactory.registerSingleton(m.getId(), m)
        );

        Set<Class<? extends TriggerFactory>> triggerFactories = reflections.getSubTypesOf(TriggerFactory.class);
        triggerFactories.stream().map(this::instantiate).forEach(m ->
                configurableBeanFactory.registerSingleton(m.getId(), m)
        );

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
