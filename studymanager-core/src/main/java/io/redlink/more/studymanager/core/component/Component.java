package io.redlink.more.studymanager.core.component;

import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.properties.ComponentProperties;

public abstract class Component<C extends ComponentProperties> {
    protected final ComponentProperties properties;

    Component(C properties) throws ConfigurationValidationException {
        this.properties = this.validate(properties);
    }

    protected abstract C validate(C properties) throws ConfigurationValidationException;

    protected abstract void activate();

    protected abstract void deactivate();
}
