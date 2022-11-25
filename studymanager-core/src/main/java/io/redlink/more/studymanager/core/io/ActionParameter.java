package io.redlink.more.studymanager.core.io;

import java.util.Objects;

public class ActionParameter extends Parameters {

    private final long studyId;
    private final int participantId;

    public ActionParameter(long studyId, int participantId) {
        super();
        this.studyId = studyId;
        this.participantId = participantId;
    }

    public long getStudyId() {
        return studyId;
    }

    public int getParticipantId() {
        return participantId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ActionParameter that = (ActionParameter) o;
        return studyId == that.studyId && participantId == that.participantId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), studyId, participantId);
    }
}
