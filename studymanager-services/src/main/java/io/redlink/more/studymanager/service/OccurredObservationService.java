/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.core.datavalidity.ObservationDataState;
import io.redlink.more.studymanager.model.OccurredObservation;
import io.redlink.more.studymanager.repository.OccurredObservationRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

@Service
public class OccurredObservationService {

    private final OccurredObservationRepository repository;

    private static final EnumSet<ObservationDataState> COMPLETED_DATA_STATES = EnumSet.of(ObservationDataState.COMPLETE);
    private static final EnumSet<ObservationDataState> ACTIVE_DATA_STATES = EnumSet.complementOf(COMPLETED_DATA_STATES);

    public OccurredObservationService(OccurredObservationRepository repository) {
        this.repository = repository;
    }

    public OccurredObservation upsert(long studyId, int observationId, int participantId, Instant start, Instant end){
        return repository.upsert(new OccurredObservation(studyId, observationId, participantId, start, end));
    }

    public OccurredObservation update(OccurredObservation occurredObservation) {
        return repository.update(occurredObservation);
    }

    /**
     * The latest start time of any {@link OccurredObservation} for the parst studyId
     * @param studyId the study id
     * @param participantId the participantId
     * @return the latest start time or <code>null</code> if no {@link OccurredObservation} is present for the study
     */
    public Instant getLatestStartTime(long studyId, int participantId) {
        return repository.getLatestStartTime(studyId, participantId, null, null, null);
    }

    /**
     * Stream over all occurred observations for a study, where data is still missing (not in
     * {@link ObservationDataState#COMPLETE})
     * @param studyId the study id
     * @param includeInvalid if occurred observations marked as having invalid data are included
     * @return a stream over all ongoing or occurred observations where data are still missing
     */
    public Stream<OccurredObservation> streamActiveOccurredObservations(long studyId, boolean includeInvalid) {
        return repository.listOccurredObservations(
                studyId, null, null,
                includeInvalid ? null : true,
                ACTIVE_DATA_STATES);
    }

    /**
     * Stream over all occurred observations for a observation of a study, where data is still missing (not in
     * {@link ObservationDataState#COMPLETE})
     * @param studyId the study id
     * @param observationId the observation id
     * @param includeInvalid if occurred observations marked as having invalid data are included
     * @return a stream over all ongoing or occurred observations where data are still missing
     */
    public Stream<OccurredObservation> streamActiveOccurredObservationsForObservation(long studyId, int observationId, boolean includeInvalid) {
        return repository.listOccurredObservations(
                studyId, null, observationId,
                includeInvalid ? null : true,
                ACTIVE_DATA_STATES);
    }

    /**
     * Stream over all occurred observations for a participant of a study, where data is still missing (not in
     * {@link ObservationDataState#COMPLETE})
     * @param studyId the study id
     * @param participantId the participant id
     * @param includeInvalid if occurred observations marked as having invalid data are included
     * @return a stream over all ongoing or occurred observations where data are still missing
     */
    public Stream<OccurredObservation> streamActiveOccurredObservationsForParticipant(long studyId, int participantId, boolean includeInvalid) {
        return repository.listOccurredObservations(
                studyId, participantId, null,
                includeInvalid ? null : true,
                ACTIVE_DATA_STATES);
    }
    /**
     * Stream over all occurred observations for a participant of a study, where data is still missing (not in
     * {@link ObservationDataState#COMPLETE})
     * @param studyId the study id
     * @param participantId the participant id or <code>null</code> as wildcard
     * @param observationId the observation id  or <code>null</code> as wildcard
     * @param includeInvalid if occurred observations marked as having invalid data are included
     * @param observationDataStates the states to include or <code>null</code> to include all
     * @return a stream over all ongoing or occurred observations where data are still missing
     */
    public Stream<OccurredObservation> streamOccurredObservations(long studyId, Integer participantId, Integer observationId, boolean includeInvalid, Set<ObservationDataState> observationDataStates) {
        return repository.listOccurredObservations(
                studyId, participantId, observationId,
                includeInvalid ? null : true,
                observationDataStates == null ? EnumSet.allOf(ObservationDataState.class) : observationDataStates);
    }

}
