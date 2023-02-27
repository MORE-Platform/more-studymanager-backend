package io.redlink.more.studymanager.core.component;

import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.properties.ComponentProperties;

public abstract class Component<C extends ComponentProperties> {
    protected final C properties;

    Component(C properties) throws ConfigurationValidationException {
        this.properties = properties;
    }

    public abstract void activate();

    public abstract void deactivate();

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "properties=" + properties +
                '}';
    }
}
