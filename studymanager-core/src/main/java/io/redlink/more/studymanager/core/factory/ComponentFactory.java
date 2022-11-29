package io.redlink.more.studymanager.core.factory;

import io.redlink.more.studymanager.core.component.Component;
import io.redlink.more.studymanager.core.properties.ComponentProperties;
import io.redlink.more.studymanager.core.webcomponent.WebComponent;

import java.util.Map;

public abstract class ComponentFactory<C extends Component, P extends ComponentProperties> {

    public abstract String getId();

    public abstract String getTitle();

    public abstract <P extends  ComponentProperties> Class<P> getPropertyClass();

    public abstract String getDescription();

    public abstract P validate(P properties);

    public Map<String,Object> getDefaultProperties() {
        return Map.of();
    }

    public WebComponent getWebComponent() {
        return null;
    }

    public boolean hasWebComponent() {
        return getWebComponent() != null;
    }

}
