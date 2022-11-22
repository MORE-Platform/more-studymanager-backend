package io.redlink.more.studymanager.core.properties;

import java.util.HashMap;
import java.util.Map;

public class ComponentProperties extends HashMap<String, Object> {
    private static final String ACTIVE = "active";
    public ComponentProperties() {
        super();
    }

    public ComponentProperties(Map<String, Object> map) {
        super(map);
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(this.get(ACTIVE));
    }

    public String getString(String name) {
        return (String) get(name);
    }

    public Integer getInt(String name) {
        return (Integer) get(name);
    }

    public Long getLong(String name) {
        return (Long) get(name);
    }
}
