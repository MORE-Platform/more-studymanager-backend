package io.redlink.more.studymanager.model;

import io.redlink.more.studymanager.model.scheduler.Duration;

import java.time.Instant;

public class StudyGroup {
    private Long studyId;
    private Integer studyGroupId;
    private String title;
    private String purpose;
    private Duration duration;
    private Instant created;
    private Instant modified;

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

    public Duration getDuration() {
        return duration;
    }

    public StudyGroup setDuration(Duration duration) {
        this.duration = duration;
        return this;
    }

    public Instant getCreated() {
        return created;
    }

    public StudyGroup setCreated(Instant created) {
        this.created = created;
        return this;
    }

    public Instant getModified() {
        return modified;
    }

    public StudyGroup setModified(Instant modified) {
        this.modified = modified;
        return this;
    }
}
