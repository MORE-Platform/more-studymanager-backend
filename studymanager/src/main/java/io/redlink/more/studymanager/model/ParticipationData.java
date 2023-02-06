package io.redlink.more.studymanager.model;

import java.time.OffsetDateTime;

public class ParticipationData {
    private Integer observationId;
    private Integer participantId;
    private Integer studyGroupId;
    private boolean dataReceived;
    private OffsetDateTime lastDataReceived;



    public Integer getObservationId() {
        return observationId;
    }

    public ParticipationData setObservationId(Integer observationId) {
        this.observationId = observationId;
        return this;
    }

    public Integer getParticipantId() {
        return participantId;
    }

    public ParticipationData setParticipantId(Integer participantId) {
        this.participantId = participantId;
        return this;
    }

    public Integer getStudyGroupId() {
        return studyGroupId;
    }

    public ParticipationData setStudyGroupId(Integer studyGroupId) {
        this.studyGroupId = studyGroupId;
        return this;
    }

    public boolean isDataReceived() {
        return dataReceived;
    }

    public ParticipationData setDataReceived(boolean dataReceived) {
        this.dataReceived = dataReceived;
        return this;
    }

    public OffsetDateTime getLastDataReceived() {
        return lastDataReceived;
    }

    public ParticipationData setLastDataReceived(OffsetDateTime lastDataReceived) {
        this.lastDataReceived = lastDataReceived;
        return this;
    }
}