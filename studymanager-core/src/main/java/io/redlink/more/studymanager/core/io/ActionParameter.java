package io.redlink.more.studymanager.core.io;

public class ActionParameter {
    private final String participantId;
    private final Parameters parameters;

    public ActionParameter(String participantId, Parameters parameters) {
        this.participantId = participantId;
        this.parameters = parameters;
    }

    public String getParticipantId() {
        return participantId;
    }

    public Parameters getParameters() {
        return parameters;
    }
}
