/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model;

import java.util.Objects;

public class SaltToken {

    private Long studyId;
    private Integer participantId;
    private String salt;

    public SaltToken() {
    }

    public Long getStudyId() {
        return studyId;
    }

    public SaltToken setStudyId(Long studyId) {
        this.studyId = studyId;
        return this;
    }

    public Integer getParticipantId() {
        return participantId;
    }

    public SaltToken setParticipantId(Integer participantId) {
        this.participantId = participantId;
        return this;
    }

    public String getSalt() {
        return salt;
    }

    public SaltToken setSalt(String salt) {
        this.salt = salt;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SaltToken that = (SaltToken) o;
        return Objects.equals(studyId, that.studyId) && Objects.equals(participantId, that.participantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studyId, participantId);
    }

    @Override
    public String toString() {
        return "SaltToken{" +
                "studyId=" + studyId +
                ", participantId=" + participantId +
                '}';
    }
}
