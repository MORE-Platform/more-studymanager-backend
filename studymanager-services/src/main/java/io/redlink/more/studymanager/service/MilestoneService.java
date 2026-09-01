/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.exception.DataConstraintException;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Milestone;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.repository.MilestoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MilestoneService {

    private final StudyStateService studyStateService;
    private final MilestoneRepository repository;

    public MilestoneService(StudyStateService studyStateService, MilestoneRepository repository) {
        this.studyStateService = studyStateService;
        this.repository = repository;
    }

    public List<Milestone> listMilestones(long studyId) {
        return repository.listMilestonesOrderedByOrderIndexAsc(studyId);
    }

    public Milestone addMilestone(long studyId, String name) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        return repository.insert(new Milestone().setStudyId(studyId).setName(name));
    }

    public Milestone getMilestone(long studyId, int milestoneId) {
        return Optional.ofNullable(repository.getByIds(studyId, milestoneId))
                .orElseThrow(() -> NotFoundException.Milestone(studyId, milestoneId));
    }

    @Transactional
    public Milestone updateMilestone(long studyId, int milestoneId, String name, int orderIndex) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        Milestone existing = getMilestone(studyId, milestoneId);
        repository.update(new Milestone().setStudyId(studyId).setMilestoneId(milestoneId).setName(name));
        if (orderIndex != existing.getOrderIndex()) {
            reorder(studyId, milestoneId, existing.getOrderIndex(), orderIndex);
        }
        return getMilestone(studyId, milestoneId);
    }

    private void reorder(long studyId, int milestoneId, int oldIndex, int requestedNewIndex) {
        int count = repository.countByStudyId(studyId);
        int newIndex = Math.max(1, Math.min(requestedNewIndex, count));
        if (newIndex == oldIndex) {
            return;
        }
        if (newIndex > oldIndex) {
            // milestones that were between the old and new position move one place up
            repository.shiftOrderIndexRange(studyId, oldIndex + 1, newIndex, -1);
        } else {
            // milestones that were between the new and old position move one place down
            repository.shiftOrderIndexRange(studyId, newIndex, oldIndex - 1, 1);
        }
        repository.setOrderIndex(studyId, milestoneId, newIndex);
    }

    @Transactional
    public void deleteMilestone(long studyId, int milestoneId) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        Milestone milestone = getMilestone(studyId, milestoneId);
        if (repository.countActiveParticipantMilestones(studyId, milestoneId) > 0) {
            throw DataConstraintException.createMilestoneInUseByActiveParticipant(studyId, milestoneId);
        }
        repository.deleteById(studyId, milestoneId);
        repository.decrementOrderIndexAbove(studyId, milestone.getOrderIndex());
    }
}
