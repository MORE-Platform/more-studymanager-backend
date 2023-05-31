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
