/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.event.ParticipantMilestoneChangedEvent;
import io.redlink.more.studymanager.exception.DataConstraintException;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.ParticipantMilestone;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.repository.MilestoneRepository;
import io.redlink.more.studymanager.repository.ParticipantMilestoneRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ParticipantMilestoneService {

    private final StudyStateService studyStateService;
    private final ParticipantMilestoneRepository repository;
    private final MilestoneRepository milestoneRepository;
    private final ParticipantService participantService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ParticipantMilestoneService(
            StudyStateService studyStateService,
            ParticipantMilestoneRepository repository,
            MilestoneRepository milestoneRepository,
            ParticipantService participantService,
            ApplicationEventPublisher applicationEventPublisher) {
        this.studyStateService = studyStateService;
        this.repository = repository;
        this.milestoneRepository = milestoneRepository;
        this.participantService = participantService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public List<ParticipantMilestone> listParticipantMilestones(long studyId, int participantId) {
        return repository.listByParticipant(studyId, participantId);
    }

    public List<ParticipantMilestone> listParticipantsForMilestone(long studyId, int milestoneId) {
        return repository.listByMilestone(studyId, milestoneId);
    }

    public ParticipantMilestone getParticipantMilestone(long studyId, int participantId, int milestoneId) {
        return findParticipantMilestone(studyId, participantId, milestoneId)
                .orElseThrow(() -> NotFoundException.ParticipantMilestone(studyId, participantId, milestoneId));
    }

    public Optional<ParticipantMilestone> findParticipantMilestone(long studyId, int participantId, int milestoneId) {
        return Optional.ofNullable(repository.getByIds(studyId, participantId, milestoneId));
    }

    public ParticipantMilestone createParticipantMilestone(long studyId, int participantId, int milestoneId, Instant dateTime) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        if (milestoneRepository.getByIds(studyId, milestoneId) == null) {
            throw NotFoundException.Milestone(studyId, milestoneId);
        }
        Participant participant = getParticipantOrThrow(studyId, participantId);
        if (repository.exists(studyId, participantId, milestoneId)) {
            throw DataConstraintException.createParticipantMilestoneAlreadyExists(studyId, participantId, milestoneId);
        }
        ParticipantMilestone created = repository.insert(new ParticipantMilestone()
                .setStudyId(studyId)
                .setParticipantId(participantId)
                .setMilestoneId(milestoneId)
                .setDateTime(dateTime));
        publishIfActive(participant);
        return created;
    }

    public ParticipantMilestone updateParticipantMilestone(long studyId, int participantId, int milestoneId, Instant dateTime) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        getParticipantMilestone(studyId, participantId, milestoneId);
        Participant participant = getParticipantOrThrow(studyId, participantId);
        ParticipantMilestone updated = repository.update(new ParticipantMilestone()
                .setStudyId(studyId)
                .setParticipantId(participantId)
                .setMilestoneId(milestoneId)
                .setDateTime(dateTime));
        publishIfActive(participant);
        return updated;
    }

    public void deleteParticipantMilestone(long studyId, int participantId, int milestoneId) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        getParticipantMilestone(studyId, participantId, milestoneId);
        repository.deleteByIds(studyId, participantId, milestoneId);
    }

    private Participant getParticipantOrThrow(long studyId, int participantId) {
        return Optional.ofNullable(participantService.getParticipant(studyId, participantId))
                .orElseThrow(() -> NotFoundException.Participant(studyId, participantId));
    }

    private void publishIfActive(Participant participant) {
        if (participant.getStatus() == Participant.Status.ACTIVE) {
            applicationEventPublisher.publishEvent(new ParticipantMilestoneChangedEvent(this, participant));
        }
    }
}
