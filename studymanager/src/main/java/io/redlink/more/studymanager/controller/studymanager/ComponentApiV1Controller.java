package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.mmb.api.v1.studymanager.model.ComponentFactoryDTO;
import io.redlink.more.mmb.api.v1.studymanager.webservices.ComponentsApi;
import io.redlink.more.studymanager.core.factory.ActionFactory;
import io.redlink.more.studymanager.core.factory.ComponentFactory;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.factory.TriggerFactory;
import io.redlink.more.studymanager.core.webcomponent.WebComponent;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class ComponentApiV1Controller implements ComponentsApi {

    private final Map<String, ObservationFactory> measurementFactories;
    private final Map<String, TriggerFactory> triggertFactories;
    private final Map<String, ActionFactory> actionFactories;

    public ComponentApiV1Controller(
            Map<String, ObservationFactory> measurementFactories,
            Map<String, TriggerFactory> triggertFactories,
            Map<String, ActionFactory> actionFactories
    ) {
        this.measurementFactories = measurementFactories;
        this.triggertFactories = triggertFactories;
        this.actionFactories = actionFactories;
    }

    @Override
    public ResponseEntity<List<ComponentFactoryDTO>> listComponents(String componentType) {
        return switch (componentType) {
            case "measurement" -> ResponseEntity.ok(measurementFactories.values().stream().map(this::toComponentDTO).toList());
            case "trigger" -> ResponseEntity.ok(triggertFactories.values().stream().map(this::toComponentDTO).toList());
            case "action" -> ResponseEntity.ok(actionFactories.values().stream().map(this::toComponentDTO).toList());
            default -> ResponseEntity.notFound().build();
        };
    }

    @Override
    public ResponseEntity<String> getWebComponentScript(String componentType, String componentId) {
        return switch (componentType) {
            case "measurement" -> getWebComponentScript(measurementFactories, componentId);
            case "trigger" -> getWebComponentScript(triggertFactories, componentId);
            case "action" -> getWebComponentScript(actionFactories, componentId);
            default -> ResponseEntity.notFound().build();
        };
    }

    private ResponseEntity<String> getWebComponentScript(Map<String, ? extends ComponentFactory> measurementFactories, String componentId) {
        if(measurementFactories.containsKey(componentId) && measurementFactories.get(componentId).hasWebComponent()) {
            return ResponseEntity.ok(
                    toScript(componentId, measurementFactories.get(componentId).getWebComponent())
            );
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private String toScript(String componentId, WebComponent webComponent) {
        return webComponent.getScript() + "\n" +
                String.format(
                        "customElements.define( 'webcomponent-%s', %s );",
                        componentId,
                        webComponent.getClassName()
                );
    }

    private ComponentFactoryDTO toComponentDTO(ComponentFactory factory) {
        return new ComponentFactoryDTO()
                .componentId(factory.getId())
                .title(factory.getTitle())
                .description(factory.getDescription())
                .hasWebComponent(factory.hasWebComponent());
    }
}
