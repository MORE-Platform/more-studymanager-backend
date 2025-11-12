/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model;

import java.util.List;
import java.util.Map;

public class StudyImportExport {

    private Study study;
    private List<StudyGroup> studyGroups;
    private List<Observation> observations;
    private List<Intervention> interventions;
    private List<ParticipantInfo> participants;
    private Map<Integer, Trigger> triggers;
    private Map<Integer, List<Action>> actions;
    private List<IntegrationInfo> integrations;

    public Study getStudy() {
        return study;
    }

    public StudyImportExport setStudy(Study study) {
        this.study = study;
        return this;
    }

    public List<StudyGroup> getStudyGroups() {
        return studyGroups;
    }

    public StudyImportExport setStudyGroups(List<StudyGroup> studyGroups) {
        this.studyGroups = studyGroups;
        return this;
    }

    public List<Observation> getObservations() {
        return observations;
    }

    public StudyImportExport setObservations(List<Observation> observations) {
        this.observations = observations;
        return this;
    }

    public List<Intervention> getInterventions() {
        return interventions;
    }

    public StudyImportExport setInterventions(List<Intervention> interventions) {
        this.interventions = interventions;
        return this;
    }

    public Map<Integer, Trigger> getTriggers() {
        return triggers;
    }

    public StudyImportExport setTriggers(Map<Integer, Trigger> triggers) {
        this.triggers = triggers;
        return this;
    }

    public Map<Integer, List<Action>> getActions() {
        return actions;
    }

    public StudyImportExport setActions(Map<Integer, List<Action>> actions) {
        this.actions = actions;
        return this;
    }

    public List<ParticipantInfo> getParticipants() {
        return participants;
    }

    public StudyImportExport setParticipants(List<ParticipantInfo> participants) {
        this.participants = participants;
        return this;
    }

    public List<IntegrationInfo> getIntegrations() {
        return integrations;
    }

    public StudyImportExport setIntegrations(List<IntegrationInfo> integrations) {
        this.integrations = integrations;
        return this;
    }

    public record ParticipantInfo(
            Integer groupId
    ) {}
}
