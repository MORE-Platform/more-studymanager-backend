/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.ObservationGroup;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.repository.ObservationGroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ObservationGroupService {

    StudyStateService studyStateService;
    private final ObservationGroupRepository repository;

    public ObservationGroupService(StudyStateService studyStateService, ObservationGroupRepository repository) {
        this.studyStateService = studyStateService;
        this.repository = repository;
    }

    public ObservationGroup createObservationGroup(ObservationGroup observationGroup) {
        studyStateService.assertStudyNotInState(observationGroup.getStudyId(), Study.Status.CLOSED);
        return this.repository.insert(observationGroup);
    }

    public ObservationGroup importObservationGroup(Long studyId, ObservationGroup observationGroupGroup) {
        return this.repository.doImport(studyId, observationGroupGroup);
    }

    public List<ObservationGroup> listObservationGroups(long studyId) {
        return this.repository.listObservationGroupsOrderedByObservationGroupIdAsc(studyId);
    }

    public ObservationGroup getObservationGroup(long studyId, int observationGroupId) {
        return Optional.ofNullable(repository.getByIds(studyId, observationGroupId))
                .orElseThrow(() -> NotFoundException.ObservationGroup(studyId, observationGroupId));
    }

    public ObservationGroup updateObservationGroup(ObservationGroup observationGroup) {
        studyStateService.assertStudyNotInState(observationGroup.getStudyId(), Study.Status.CLOSED);
        return this.repository.update(observationGroup);
    }

    public void deleteObservationGroup(long studyId, int observationGroupId) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        this.repository.deleteById(studyId, observationGroupId);
    }

    public void addObservationToGroup(long studyId, int observationGroupId, int observationId) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        getObservationGroup(studyId, observationGroupId);
        repository.addObservationToGroup(studyId, observationId, observationGroupId);
    }

    public void addInterventionToGroup(long studyId, int observationGroupId, int interventionId) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        getObservationGroup(studyId, observationGroupId);
        repository.addInterventionToGroup(studyId, interventionId, observationGroupId);
    }

    public void addParticipantToGroup(long studyId, int observationGroupId, int participantId) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        getObservationGroup(studyId, observationGroupId);
        repository.addParticipantToGroup(studyId, participantId, observationGroupId);
    }

    public void removeObservationFromGroup(long studyId, int observationGroupId, int observationId) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        getObservationGroup(studyId, observationGroupId);
        repository.removeObservationFromGroup(studyId, observationId, observationGroupId);
    }

    public void removeInterventionFromGroup(long studyId, int observationGroupId, int interventionId) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        getObservationGroup(studyId, observationGroupId);
        repository.removeInterventionFromGroup(studyId, interventionId, observationGroupId);
    }

    public void removeParticipantFromGroup(long studyId, int observationGroupId, int participantId) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        getObservationGroup(studyId, observationGroupId);
        repository.removeParticipantFromGroup(studyId, participantId, observationGroupId);
    }

    public int countObservationsInGroup(long studyId, int observationGroupId) {
        //getObservationGroup(studyId, observationGroupId);
        return repository.countObservationsInGroup(studyId, observationGroupId);
    }

    public int countInterventionsInGroup(long studyId, int observationGroupId) {
        //getObservationGroup(studyId, observationGroupId);
        return repository.countInterventionsInGroup(studyId, observationGroupId);
    }

    public int countParticipantsInGroup(long studyId, int observationGroupId) {
        //getObservationGroup(studyId, observationGroupId);
        return repository.countParticipantsInGroup(studyId, observationGroupId);
    }

}
