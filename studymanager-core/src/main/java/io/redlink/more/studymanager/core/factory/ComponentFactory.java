package io.redlink.more.studymanager.core.factory;

import io.redlink.more.studymanager.core.component.Component;
import io.redlink.more.studymanager.core.properties.ComponentProperties;
import io.redlink.more.studymanager.core.webcomponent.WebComponent;

public abstract class ComponentFactory<C extends Component, P extends ComponentProperties> {

    public abstract String getId();

    public abstract String getTitle();

    public abstract String getDescription();

    public WebComponent getWebComponent() {
        return null;
    }

    public boolean hasWebComponent() {
        return getWebComponent() != null;
    }

}
