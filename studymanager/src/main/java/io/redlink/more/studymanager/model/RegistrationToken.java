package io.redlink.more.studymanager.model;

public class RegistrationToken {
    private Long studyId;
    private Integer participantId;
    private String token;

    public Long getStudyId() {
        return studyId;
    }

    public RegistrationToken setStudyId(Long studyId) {
        this.studyId = studyId;
        return this;
    }

    public Integer getParticipantId() {
        return participantId;
    }

    public RegistrationToken setParticipantId(Integer participantId) {
        this.participantId = participantId;
        return this;
    }

    public String getToken() {
        return token;
    }

    public RegistrationToken setToken(String token) {
        this.token = token;
        return this;
    }
}
