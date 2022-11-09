package io.redlink.more.studymanager.model;

import java.sql.Timestamp;

public class Observation {
    private Long studyId;
    private Integer observationId;
    private String title;
    private String purpose;
    private String participantInfo;
    private String type;
    private Integer studyGroupId;
    private Object properties;
    private Object schedule;
    private Timestamp created;
    private Timestamp modified;


    public Long getStudyId() {
        return studyId;
    }

    public Observation setStudyId(Long studyId) {
        this.studyId = studyId;
        return this;
    }

    public Integer getObservationId() {
        return observationId;
    }

    public Observation setObservationId(Integer observationId) {
        this.observationId = observationId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Observation setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getPurpose() {
        return purpose;
    }

    public Observation setPurpose(String purpose) {
        this.purpose = purpose;
        return this;
    }

    public String getParticipantInfo() {
        return participantInfo;
    }

    public Observation setParticipantInfo(String participantInfo) {
        this.participantInfo = participantInfo;
        return this;
    }

    public String getType() {
        return type;
    }

    public Observation setType(String type) {
        this.type = type;
        return this;
    }

    public Integer getStudyGroupId() {
        return studyGroupId;
    }

    public Observation setStudyGroupId(Integer studyGroupId) {
        this.studyGroupId = studyGroupId;
        return this;
    }

    public Object getProperties() {
        return properties;
    }

    public Observation setProperties(Object properties) {
        this.properties = properties;
        return this;
    }

    public Object getSchedule() {
        return schedule;
    }

    public Observation setSchedule(Object schedule) {
        this.schedule = schedule;
        return this;
    }

    public Timestamp getCreated() {
        return created;
    }

    public Observation setCreated(Timestamp created) {
        this.created = created;
        return this;
    }

    public Timestamp getModified() {
        return modified;
    }

    public Observation setModified(Timestamp modified) {
        this.modified = modified;
        return this;
    }
}
