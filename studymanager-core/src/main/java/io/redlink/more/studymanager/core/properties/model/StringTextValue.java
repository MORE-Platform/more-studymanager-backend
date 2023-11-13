/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.core.properties.model;

public class StringTextValue extends Value<String> {
    public StringTextValue(String id) {
        super(id);
    }

    @Override
    public String getType() {
        return "STRINGTEXT";
    }

    @Override
    public Class<String> getValueType() {
        return String.class;
    }
}
