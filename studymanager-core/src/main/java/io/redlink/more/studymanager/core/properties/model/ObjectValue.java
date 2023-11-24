/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.core.properties.model;

public class ObjectValue extends Value<Object> {
    public ObjectValue(String id) {
        super(id);
    }

    @Override
    public Class<Object> getValueType() {
        return Object.class;
    }

    @Override
    public String getType() {
        return "OBJECT";
    }
}
