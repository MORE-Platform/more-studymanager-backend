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

public class ValueCastException extends RuntimeException implements ValueException {

    private Value value;

    public <T> ValueCastException(Value<T> value, Class clazz) {
        super("Value must be a valid " + clazz.getSimpleName());
        this.value = value;
    }

    public Value getValue() {
        return value;
    }
}
