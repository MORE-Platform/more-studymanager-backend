package io.redlink.more.studymanager.model;

import java.sql.Timestamp;
import java.time.Instant;

public class Intervention {
    private Long studyId;
    private Integer interventionId;
    private String title;
    private String purpose;
    private Integer studyGroupId;
    private Object schedule;
    private Instant created;
    private Instant modified;

    public Long getStudyId() {
        return studyId;
    }

    public Intervention setStudyId(Long studyId) {
        this.studyId = studyId;
        return this;
    }

    public Integer getInterventionId() {
        return interventionId;
    }

    public Intervention setInterventionId(Integer interventionId) {
        this.interventionId = interventionId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Intervention setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getPurpose() {
        return purpose;
    }

    public Intervention setPurpose(String purpose) {
        this.purpose = purpose;
        return this;
    }

    public Integer getStudyGroupId() {
        return studyGroupId;
    }

    public Intervention setStudyGroupId(Integer studyGroupId) {
        this.studyGroupId = studyGroupId;
        return this;
    }

    public Object getSchedule() {
        return schedule;
    }

    public Intervention setSchedule(Object schedule) {
        this.schedule = schedule;
        return this;
    }

    public Instant getCreated() {
        return created;
    }

    public Intervention setCreated(Instant created) {
        this.created = created;
        return this;
    }

    public Instant getModified() {
        return modified;
    }

    public Intervention setModified(Instant modified) {
        this.modified = modified;
        return this;
    }
}
