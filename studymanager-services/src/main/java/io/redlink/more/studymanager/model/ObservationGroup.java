/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model;

import io.redlink.more.studymanager.model.scheduler.Duration;

import java.time.Instant;

public class ObservationGroup {
    private Long studyId;
    private Integer observationGroupId;
    private String title;
    private String purpose;
    private Instant created;
    private Instant modified;

    public Long getStudyId() {
        return studyId;
    }

    public ObservationGroup setStudyId(Long studyId) {
        this.studyId = studyId;
        return this;
    }

    public Integer getObservationGroupId() {
        return observationGroupId;
    }

    public ObservationGroup setObservationGroupId(Integer observationGroupId) {
        this.observationGroupId = observationGroupId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public ObservationGroup setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getPurpose() {
        return purpose;
    }

    public ObservationGroup setPurpose(String purpose) {
        this.purpose = purpose;
        return this;
    }

    public Instant getCreated() {
        return created;
    }

    public ObservationGroup setCreated(Instant created) {
        this.created = created;
        return this;
    }

    public Instant getModified() {
        return modified;
    }

    public ObservationGroup setModified(Instant modified) {
        this.modified = modified;
        return this;
    }
}
