/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.core.io;

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
