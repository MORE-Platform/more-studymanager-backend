/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.core.exception;

import io.redlink.more.studymanager.core.properties.model.Value;

public class ValueNonNullException extends RuntimeException implements ValueException {

    private Value value;

    public <T> ValueNonNullException(Value<T> value) {
        super("Value must be set");
        this.value = value;
    }

    public Value getValue() {
        return value;
    }
}
