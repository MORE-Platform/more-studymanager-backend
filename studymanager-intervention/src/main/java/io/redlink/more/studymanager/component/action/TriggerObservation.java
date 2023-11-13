/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.component.action;

public class TriggerObservation {
    private Integer id;
    private String factory;

    public Integer getId() {
        return id;
    }

    public TriggerObservation setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getFactory() {
        return factory;
    }

    public TriggerObservation setFactory(String factory) {
        this.factory = factory;
        return this;
    }
}
