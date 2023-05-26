package io.redlink.more.studymanager.core.properties;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ComponentProperties extends HashMap<String, Object> {
    private static final ObjectMapper _MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    public ComponentProperties() {
        super();
    }

    public ComponentProperties(Map<String, Object> map) {
        super(map);
    }

    public String getString(String name) {
        Object o = get(name);
        if(o == null) return null;
        if(o instanceof String) return (String) o;
        else throw new ClassCastException();
    }

    public Boolean getBoolean(String name) {
        Object o = get(name);
        if(o == null) return null;
        if(o instanceof Boolean) return (Boolean) o;
        else throw new ClassCastException();
    }

    public Integer getInt(String name) {
        Object o = get(name);
        if(o == null) return null;
        if(o instanceof Integer) return (Integer) o;
        if(o instanceof Long) return ((Long)o).intValue();
        else throw new ClassCastException();
    }

    public Long getLong(String name) {
        Object o = get(name);
        if(o == null) return null;
        if(o instanceof Long) return (Long) o;
        if(o instanceof Integer) return ((Integer)o).longValue();
        else throw new ClassCastException();
    }

    public <T> Optional<T> getObject(String name, TypeReference<T> clazz) {
        return Optional.ofNullable(this.get(name)).map(v -> _MAPPER.convertValue(v, clazz));
    }
}
