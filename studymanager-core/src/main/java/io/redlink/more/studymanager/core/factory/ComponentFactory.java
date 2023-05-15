package io.redlink.more.studymanager.core.factory;

import com.fasterxml.jackson.databind.JsonNode;
import io.redlink.more.studymanager.core.component.Component;
import io.redlink.more.studymanager.core.exception.ApiCallException;
import io.redlink.more.studymanager.core.model.User;
import io.redlink.more.studymanager.core.properties.ComponentProperties;
import io.redlink.more.studymanager.core.webcomponent.WebComponent;
import java.util.Map;

public abstract class ComponentFactory<C extends Component, P extends ComponentProperties> {
    public ComponentFactoryProperties componentProperties;
    public ComponentFactory<C,P> init(ComponentFactoryProperties componentProperties){
        this.componentProperties = componentProperties;
        return this;
    }
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

    public JsonNode handleAPICall(String slug, User user, JsonNode input) throws ApiCallException {
        throw new ApiCallException(404, "Not found");
    }

    public boolean hasWebComponent() {
        return getWebComponent() != null;
    }

}
