package io.redlink.more.studymanager.repository;

import java.io.Serializable;

public class SampleObject implements Serializable {
    private String value;

    public SampleObject(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
