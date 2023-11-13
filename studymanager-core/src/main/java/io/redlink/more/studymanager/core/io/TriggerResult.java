/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.core.io;

import java.util.Set;

public class TriggerResult {

    private final Set<ActionParameter> actionParameters;
    private final boolean proceed;

    public TriggerResult(Set<ActionParameter> actionParameters, boolean proceed) {
        this.actionParameters = actionParameters;
        this.proceed = proceed;
    }

    public static TriggerResult NOOP = new TriggerResult(null, false);
    public static TriggerResult withParams(Set<ActionParameter> actionParameterSet) {
        return new TriggerResult(actionParameterSet, true);
    }

    public Set<ActionParameter> getActionParameters() {
        return actionParameters;
    }

    public boolean proceed() {
        return proceed;
    }
}
