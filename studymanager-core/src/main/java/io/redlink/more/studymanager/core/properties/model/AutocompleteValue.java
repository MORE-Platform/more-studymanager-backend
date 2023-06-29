package io.redlink.more.studymanager.core.properties.model;

public class AutocompleteValue extends StringValue {
    private final String endpointSlug;
    public AutocompleteValue(String id, String endpointSlug) {
        super(id);
        this.endpointSlug = endpointSlug;
    }
}
