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
