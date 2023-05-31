package io.redlink.more.studymanager.core.properties.model;

import io.redlink.more.studymanager.core.validation.ValidationIssue;

public class IntegerValue extends Value<Integer> {

    private int min = 0;
    private int max = Integer.MAX_VALUE;

    public IntegerValue(String id) {
        super(id);
    }

    @Override
    public String getType() {
        return "INTEGER";
    }

    @Override
    public Class<Integer> getValueType() {
        return Integer.class;
    }

    @Override
    public ValidationIssue validate(Integer integer) {
        if(integer != null && (integer < getMin() || integer > getMax())) {
            return ValidationIssue.error(this, "Value must between " + getMin() + " and " + getMax());
        }
        return validationFunction != null ? validationFunction.apply(integer) : ValidationIssue.NONE;
    }

    public int getMin() {
        return min;
    }

    public IntegerValue setMin(int min) {
        this.min = min;
        return this;
    }

    public int getMax() {
        return max;
    }

    public IntegerValue setMax(int max) {
        this.max = max;
        return this;
    }
}
