/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.exception.DataConstraintException;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.ObservationResyncRequest;
import io.redlink.more.studymanager.repository.ObservationResyncRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Manages requests to re-collect observation data that is missing or incomplete. A request is a row that
 * the storage gateway picks up and deletes once it has synced the data, so the presence of a row means
 * "resync pending".
 */
@Service
public class ObservationResyncService {

    private final ObservationResyncRequestRepository repository;
    private final ParticipantService participantService;
    private final ObservationService observationService;

    public ObservationResyncService(
            ObservationResyncRequestRepository repository,
            ParticipantService participantService,
            ObservationService observationService) {
        this.repository = repository;
        this.participantService = participantService;
        this.observationService = observationService;
    }

    public Optional<ObservationResyncRequest> find(long studyId, int participantId, int observationId) {
        return repository.find(studyId, participantId, observationId);
    }

    public List<ObservationResyncRequest> listByParticipant(long studyId, int participantId) {
        return repository.listByParticipant(studyId, participantId);
    }

    /**
     * Whether the factory backing the given observation supports re-collecting its data.
     *
     * @throws NotFoundException if the observation does not exist in the study
     */
    public boolean isResyncable(long studyId, int observationId) {
        return isResyncable(getObservationOrThrow(studyId, observationId));
    }

    public ObservationResyncRequest requestResync(long studyId, int participantId, int observationId) {
        if (participantService.getParticipant(studyId, participantId) == null) {
            throw NotFoundException.Participant(studyId, participantId);
        }
        Observation observation = getObservationOrThrow(studyId, observationId);
        if (!isResyncable(observation)) {
            throw new BadRequestException(
                    "Observation %d of type '%s' in study %d does not support resync"
                            .formatted(observationId, observation.getType(), studyId));
        }
        if (repository.find(studyId, participantId, observationId).isPresent()) {
            throw DataConstraintException.createObservationResyncRequestAlreadyExists(studyId, participantId, observationId);
        }
        return repository.insert(studyId, participantId, observationId);
    }

    private Observation getObservationOrThrow(long studyId, int observationId) {
        return observationService.getObservation(studyId, observationId)
                .orElseThrow(() -> NotFoundException.Observation(studyId, observationId));
    }

    private boolean isResyncable(Observation observation) {
        return observationService.getObservationFactory(observation)
                .map(ObservationFactory::isResyncable)
                .orElse(false);
    }
}
