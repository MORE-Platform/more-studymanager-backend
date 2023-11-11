/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.core.properties.model;

import java.util.List;

public class ChoiceValue extends Value<String> {
    private List<String> options;

    public ChoiceValue(String id) {
        super(id);
    }

    @Override
    public String getType() {
        return "CHOICE";
    }

    @Override
    public Class<String> getValueType() {
        return String.class;
    }
}
