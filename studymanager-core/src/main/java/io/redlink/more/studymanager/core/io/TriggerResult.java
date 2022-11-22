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
    public static TriggerResult PROCEED_ALL = new TriggerResult(null, true);

    public static TriggerResult withParams(Set<ActionParameter> actionParameterSet) {
        return new TriggerResult(actionParameterSet, true);
    }

    public Set<ActionParameter> getActionParameters() {
        return actionParameters;
    }

    public boolean isProceed() {
        return proceed;
    }
}
