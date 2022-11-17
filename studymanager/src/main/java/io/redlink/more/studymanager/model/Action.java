package io.redlink.more.studymanager.model;

import io.redlink.more.studymanager.core.properties.ActionProperties;

import java.time.Instant;

public class Action {
    private Integer actionId;
    private String type;
    private ActionProperties properties;
    private Instant created;
    private Instant modified;

    public Integer getActionId() {
        return actionId;
    }

    public Action setActionId(Integer actionId) {
        this.actionId = actionId;
        return this;
    }

    public String getType() {
        return type;
    }

    public Action setType(String type) {
        this.type = type;
        return this;
    }

    public ActionProperties getProperties() {
        return properties;
    }

    public Action setProperties(ActionProperties properties) {
        this.properties = properties;
        return this;
    }

    public Instant getCreated() {
        return created;
    }

    public Action setCreated(Instant created) {
        this.created = created;
        return this;
    }

    public Instant getModified() {
        return modified;
    }

    public Action setModified(Instant modified) {
        this.modified = modified;
        return this;
    }
}
