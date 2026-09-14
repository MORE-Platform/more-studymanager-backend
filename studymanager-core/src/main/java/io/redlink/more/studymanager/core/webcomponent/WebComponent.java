/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.studymanager.core.webcomponent;

public class WebComponent {
    private String className;
    private String script;

    public WebComponent(String className, String script) {
        this.className = className;
        this.script = script;
    }

    public String getClassName() {
        return className;
    }

    public String getScript() {
        return script;
    }
}
