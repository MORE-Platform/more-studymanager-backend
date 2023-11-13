/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model;
import java.time.Instant;

public class Participant {
    private Long studyId;
    private Integer participantId;
    private String alias;
    private Integer studyGroupId;
    private Status status;
    private Instant created;
    private Instant modified;

    private String registrationToken;

    public Long getStudyId() {
        return this.studyId;
    }

    public Status getStatus() {
        return status;
    }

    public enum Status {
        NEW("new"),
        ACTIVE("active"),
        ABANDONED("abandoned"),
        KICKED_OUT("kicked_out"),
        LOCKED("locked");

        private final String value;

        Status(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public Participant setStatus(Status status) {
        this.status = status;
        return this;
    }

    public Participant setStudyId(Long studyId) {
        this.studyId = studyId;
        return this;
    }

    public Integer getParticipantId() {
        return participantId;
    }

    public Participant setParticipantId(Integer participantId) {
        this.participantId = participantId;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public Participant setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public Integer getStudyGroupId() {
        return studyGroupId;
    }

    public Participant setStudyGroupId(Integer studyGroupId) {
        this.studyGroupId = studyGroupId;
        return this;
    }

    public Instant getCreated() {
        return created;
    }

    public Participant setCreated(Instant created) {
        this.created = created;
        return this;
    }

    public Instant getModified() {
        return modified;
    }

    public Participant setModified(Instant modified) {
        this.modified = modified;
        return this;
    }

    public String getRegistrationToken() {
        return registrationToken;
    }

    public Participant setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
        return this;
    }
}
