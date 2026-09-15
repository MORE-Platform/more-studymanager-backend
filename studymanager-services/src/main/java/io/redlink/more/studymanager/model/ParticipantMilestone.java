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

public class ParticipantMilestone {
    private Long studyId;
    private Integer participantId;
    private Integer milestoneId;
    private Integer participantMilestoneId;
    private String name;
    private Instant dateTime;
    private Instant created;
    private Instant modified;

    public Long getStudyId() {
        return studyId;
    }

    public ParticipantMilestone setStudyId(Long studyId) {
        this.studyId = studyId;
        return this;
    }

    public Integer getParticipantId() {
        return participantId;
    }

    public ParticipantMilestone setParticipantId(Integer participantId) {
        this.participantId = participantId;
        return this;
    }

    public Integer getMilestoneId() {
        return milestoneId;
    }

    public ParticipantMilestone setMilestoneId(Integer milestoneId) {
        this.milestoneId = milestoneId;
        return this;
    }

    public Integer getParticipantMilestoneId() {
        return participantMilestoneId;
    }

    public ParticipantMilestone setParticipantMilestoneId(Integer participantMilestoneId) {
        this.participantMilestoneId = participantMilestoneId;
        return this;
    }

    // the name of the referenced Milestone, not a column of participant_milestones itself
    public String getName() {
        return name;
    }

    public ParticipantMilestone setName(String name) {
        this.name = name;
        return this;
    }

    public Instant getDateTime() {
        return dateTime;
    }

    public ParticipantMilestone setDateTime(Instant dateTime) {
        this.dateTime = dateTime;
        return this;
    }

    public Instant getCreated() {
        return created;
    }

    public ParticipantMilestone setCreated(Instant created) {
        this.created = created;
        return this;
    }

    public Instant getModified() {
        return modified;
    }

    public ParticipantMilestone setModified(Instant modified) {
        this.modified = modified;
        return this;
    }
}
