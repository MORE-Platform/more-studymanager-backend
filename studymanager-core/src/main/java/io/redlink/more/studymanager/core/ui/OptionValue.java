/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.core.ui;

public class OptionValue {
    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public OptionValue setName(String name) {
        this.name = name;
        return this;
    }

    public String getValue() {
        return value;
    }

    public OptionValue setValue(String value) {
        this.value = value;
        return this;
    }
}
