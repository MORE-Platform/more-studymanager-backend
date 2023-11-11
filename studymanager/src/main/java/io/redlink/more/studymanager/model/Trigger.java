/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model;

import io.redlink.more.studymanager.core.properties.TriggerProperties;

import java.time.Instant;

public class Trigger {
    private String type;
    private TriggerProperties properties;
    private Instant created;
    private Instant modified;

    public String getType() {
        return type;
    }

    public Trigger setType(String type) {
        this.type = type;
        return this;
    }

    public TriggerProperties getProperties() {
        return properties;
    }

    public Trigger setProperties(TriggerProperties properties) {
        this.properties = properties;
        return this;
    }

    public Instant getCreated() {
        return created;
    }

    public Trigger setCreated(Instant created) {
        this.created = created;
        return this;
    }

    public Instant getModified() {
        return modified;
    }

    public Trigger setModified(Instant modified) {
        this.modified = modified;
        return this;
    }
}
