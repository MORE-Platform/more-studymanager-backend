package io.redlink.more.studymanager.model;

import java.sql.Date;
import java.sql.Timestamp;

public class Study {
    private Long studyId;
    private String title;
    private String purpose;
    private String participantInfo;
    private String consentInfo;
    private Status studyState;
    private Date startDate;
    private Date endDate;
    private Date plannedStartDate;
    private Date plannedEndDate;
    private Timestamp created;
    private Timestamp modified;

    public enum Status {
        DRAFT("draft"),
        ACTIVE("active"),
        PAUSED("paused"),
        CLOSED("closed");

        private final String value;

        Status(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public Long getStudyId() {
        return studyId;
    }

    public Study setStudyId(Long studyId) {
        this.studyId = studyId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Study setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getPurpose() {
        return purpose;
    }

    public Study setPurpose(String purpose) {
        this.purpose = purpose;
        return this;
    }

    public String getParticipantInfo() {
        return participantInfo;
    }

    public Study setParticipantInfo(String participantInfo) {
        this.participantInfo = participantInfo;
        return this;
    }

    public String getConsentInfo() {
        return consentInfo;
    }

    public Study setConsentInfo(String consentInfo) {
        this.consentInfo = consentInfo;
        return this;
    }

    public Status getStudyState() {
        return studyState;
    }

    public Study setStudyState(Status studyState) {
        this.studyState = studyState;
        return this;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Study setStartDate(Date startDate) {
        this.startDate = startDate;
        return this;
    }

    public Date getEndDate() {
        return endDate;
    }

    public Study setEndDate(Date endDate) {
        this.endDate = endDate;
        return this;
    }

    public Date getPlannedStartDate() {
        return plannedStartDate;
    }

    public Study setPlannedStartDate(Date plannedStartDate) {
        this.plannedStartDate = plannedStartDate;
        return this;
    }

    public Date getPlannedEndDate() {
        return plannedEndDate;
    }

    public Study setPlannedEndDate(Date plannedEndDate) {
        this.plannedEndDate = plannedEndDate;
        return this;
    }

    public Timestamp getCreated() {
        return created;
    }

    public Study setCreated(Timestamp created) {
        this.created = created;
        return this;
    }

    public Timestamp getModified() {
        return modified;
    }

    public Study setModified(Timestamp modified) {
        this.modified = modified;
        return this;
    }

    @Override
    public String toString() {
        return "Study{" +
                "studyId=" + studyId +
                ", title='" + title + '\'' +
                ", purpose='" + purpose + '\'' +
                ", participantInfo='" + participantInfo + '\'' +
                ", consentInfo='" + consentInfo + '\'' +
                ", studyState=" + studyState +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", plannedStartDate=" + plannedStartDate +
                ", plannedEndDate=" + plannedEndDate +
                ", created=" + created +
                ", modified=" + modified +
                '}';
    }
}
