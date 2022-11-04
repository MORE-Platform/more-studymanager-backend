package io.redlink.more.studymanager.core;

import java.util.HashMap;
import java.util.Map;

public class Parameters extends HashMap<String, Object> {

    public Parameters() {
    }

    public Parameters(Map<? extends String, ?> m) {
        super(m);
    }

    public String getString(String name) {
        return (String) get(name);
    }
}
