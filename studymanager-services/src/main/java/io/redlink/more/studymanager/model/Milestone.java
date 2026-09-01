/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.model;

import java.time.Instant;

public class Milestone {
    private Long studyId;
    private Integer milestoneId;
    private String name;
    private Integer orderIndex;
    private Instant created;

    public Long getStudyId() {
        return studyId;
    }

    public Milestone setStudyId(Long studyId) {
        this.studyId = studyId;
        return this;
    }

    public Integer getMilestoneId() {
        return milestoneId;
    }

    public Milestone setMilestoneId(Integer milestoneId) {
        this.milestoneId = milestoneId;
        return this;
    }

    public String getName() {
        return name;
    }

    public Milestone setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public Milestone setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
        return this;
    }

    public Instant getCreated() {
        return created;
    }

    public Milestone setCreated(Instant created) {
        this.created = created;
        return this;
    }
}
