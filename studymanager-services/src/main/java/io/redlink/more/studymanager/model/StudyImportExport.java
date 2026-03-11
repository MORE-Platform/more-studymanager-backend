/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StudyImportExport {

    private Study study;
    private List<StudyGroup> studyGroups = new ArrayList<>();
    private List<ObservationGroup> observationGroups = new ArrayList<>();
    private List<Observation> observations = new ArrayList<>();
    private List<Intervention> interventions = new ArrayList<>();
    private List<ParticipantInfo> participants = new ArrayList<>();
    private Map<Integer, Trigger> triggers = new HashMap<>();
    private Map<Integer, List<Action>> actions =  new HashMap<>();
    private List<IntegrationInfo> integrations = new ArrayList<>();

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
        this.studyGroups = studyGroups == null ? new ArrayList<>() : studyGroups;
        return this;
    }

    public List<ObservationGroup> getObservationGroups() {
        return observationGroups;
    }

    public StudyImportExport setObservationGroups(List<ObservationGroup> observationGroups) {
        this.observationGroups = observationGroups == null ? new ArrayList<>() : observationGroups;
        return this;
    }

    public List<Observation> getObservations() {
        return observations;
    }

    public StudyImportExport setObservations(List<Observation> observations) {
        this.observations = observations ==  null ? new ArrayList<>() : observations;
        return this;
    }

    public List<Intervention> getInterventions() {
        return interventions;
    }

    public StudyImportExport setInterventions(List<Intervention> interventions) {
        this.interventions = interventions == null ? new ArrayList<>() : interventions;
        return this;
    }

    public Map<Integer, Trigger> getTriggers() {
        return triggers;
    }

    public StudyImportExport setTriggers(Map<Integer, Trigger> triggers) {
        this.triggers = triggers == null ? new HashMap<>() : triggers;
        return this;
    }

    public Map<Integer, List<Action>> getActions() {
        return actions;
    }

    public StudyImportExport setActions(Map<Integer, List<Action>> actions) {
        this.actions = actions == null ? new HashMap<>() : actions;
        return this;
    }

    public List<ParticipantInfo> getParticipants() {
        return participants;
    }

    public StudyImportExport setParticipants(List<ParticipantInfo> participants) {
        this.participants = participants == null ? new ArrayList<>() : participants;
        return this;
    }

    public List<IntegrationInfo> getIntegrations() {
        return integrations;
    }

    public StudyImportExport setIntegrations(List<IntegrationInfo> integrations) {
        this.integrations = integrations == null ? new ArrayList<>() : integrations;
        return this;
    }

    public record ParticipantInfo(
            Integer groupId,
            Set<Integer> observationGroupIds
    ) {
        public ParticipantInfo {
            observationGroupIds = observationGroupIds == null ? Collections.emptySet() : observationGroupIds;
        }
    }
}
