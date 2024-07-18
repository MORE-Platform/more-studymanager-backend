/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.core.properties.model;

import io.redlink.more.studymanager.core.validation.ValidationIssue;

public class StringValue extends Value<String> {
    public StringValue(String id) {
        super(id);
    }

    @Override
    protected ValidationIssue doValidate(String s) {
        if (isRequired() && s.trim().isEmpty()) {
            return ValidationIssue.requiredMissing(this);
        }
        return super.doValidate(s);
    }

    @Override
    public String getType() {
        return "STRING";
    }

    @Override
    public Class<String> getValueType() {
        return String.class;
    }
}
