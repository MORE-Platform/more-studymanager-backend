package io.redlink.more.studymanager.model;

import java.sql.Timestamp;

public class StudyGroup {
    private Long studyId;
    private Integer studyGroupId;
    private String title;
    private String purpose;
    private Timestamp created;
    private Timestamp modified;

    public Long getStudyId() {
        return studyId;
    }

    public StudyGroup setStudyId(Long studyId) {
        this.studyId = studyId;
        return this;
    }

    public Integer getStudyGroupId() {
        return studyGroupId;
    }

    public StudyGroup setStudyGroupId(Integer studyGroupId) {
        this.studyGroupId = studyGroupId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public StudyGroup setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getPurpose() {
        return purpose;
    }

    public StudyGroup setPurpose(String purpose) {
        this.purpose = purpose;
        return this;
    }

    public Timestamp getCreated() {
        return created;
    }

    public StudyGroup setCreated(Timestamp created) {
        this.created = created;
        return this;
    }

    public Timestamp getModified() {
        return modified;
    }

    public StudyGroup setModified(Timestamp modified) {
        this.modified = modified;
        return this;
    }
}
