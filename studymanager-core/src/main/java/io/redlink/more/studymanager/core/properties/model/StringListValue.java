package io.redlink.more.studymanager.core.properties.model;

import java.util.List;

public class StringListValue extends Value<List> {

    int minSize = 0;
    int maxSize = 10;

    public StringListValue(String id) {
        super(id);
    }

    @Override
    public Class<List> getValueType() {
        return List.class;
    }

    @Override
    public String getType() {
        return "STRINGLIST";
    }

    public int getMinSize() {
        return minSize;
    }

    public StringListValue setMinSize(int minSize) {
        this.minSize = minSize;
        return this;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public StringListValue setMaxSize(int maxSize) {
        this.maxSize = maxSize;
        return this;
    }
}
