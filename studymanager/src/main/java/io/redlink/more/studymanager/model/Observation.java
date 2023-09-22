package io.redlink.more.studymanager.model;

import io.redlink.more.studymanager.core.properties.ObservationProperties;

import java.time.Instant;

public class Observation {
    private Long studyId;
    private Integer observationId;
    private String title;
    private String purpose;
    private String participantInfo;
    private String type;
    private Integer studyGroupId;
    private ObservationProperties properties;
    private Event schedule;
    private Instant created;
    private Instant modified;
    private Boolean hidden;
    private Boolean noSchedule = false;

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

    public ObservationProperties getProperties() {
        return properties;
    }

    public Observation setProperties(ObservationProperties properties) {
        this.properties = properties;
        return this;
    }

    public Event getSchedule() {
        return schedule;
    }

    public Observation setSchedule(Event schedule) {
        this.schedule = schedule;
        return this;
    }

    public Instant getCreated() {
        return created;
    }

    public Observation setCreated(Instant created) {
        this.created = created;
        return this;
    }

    public Instant getModified() {
        return modified;
    }

    public Observation setModified(Instant modified) {
        this.modified = modified;
        return this;
    }

    public Boolean getHidden() { return hidden; }

    public Observation setHidden(Boolean hidden) {
        this.hidden = hidden;
        return this;
    }

    public Boolean getNoSchedule() {
        return noSchedule;
    }

    public Observation setNoSchedule(Boolean noSchedule) {
        this.noSchedule = noSchedule;
        return this;
    }
}
