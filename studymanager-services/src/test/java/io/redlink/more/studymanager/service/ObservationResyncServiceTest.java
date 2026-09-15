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
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.repository.ObservationResyncRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObservationResyncServiceTest {

    private static final long STUDY_ID = 1L;
    private static final int PARTICIPANT_ID = 2;
    private static final int OBSERVATION_ID = 3;

    @Mock
    ObservationResyncRequestRepository repository;

    @Mock
    ParticipantService participantService;

    @Mock
    ObservationService observationService;

    @Mock
    ObservationFactory<?, ?> factory;

    @InjectMocks
    ObservationResyncService service;

    @Test
    void requestFailsWhenParticipantIsUnknown() {
        when(participantService.getParticipant(STUDY_ID, PARTICIPANT_ID)).thenReturn(null);

        assertThatThrownBy(() -> service.requestResync(STUDY_ID, PARTICIPANT_ID, OBSERVATION_ID))
                .isInstanceOf(NotFoundException.class);

        verify(repository, never()).insert(STUDY_ID, PARTICIPANT_ID, OBSERVATION_ID);
    }

    @Test
    void requestFailsWhenObservationIsUnknown() {
        when(participantService.getParticipant(STUDY_ID, PARTICIPANT_ID)).thenReturn(participant());
        when(observationService.getObservation(STUDY_ID, OBSERVATION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.requestResync(STUDY_ID, PARTICIPANT_ID, OBSERVATION_ID))
                .isInstanceOf(NotFoundException.class);

        verify(repository, never()).insert(STUDY_ID, PARTICIPANT_ID, OBSERVATION_ID);
    }

    @Test
    void requestFailsWhenObservationIsNotResyncable() {
        givenObservationWithFactory(false);

        assertThatThrownBy(() -> service.requestResync(STUDY_ID, PARTICIPANT_ID, OBSERVATION_ID))
                .isInstanceOf(BadRequestException.class);

        verify(repository, never()).insert(STUDY_ID, PARTICIPANT_ID, OBSERVATION_ID);
    }

    @Test
    void requestFailsWhenAlreadyPending() {
        givenObservationWithFactory(true);
        when(repository.find(STUDY_ID, PARTICIPANT_ID, OBSERVATION_ID)).thenReturn(Optional.of(request()));

        assertThatThrownBy(() -> service.requestResync(STUDY_ID, PARTICIPANT_ID, OBSERVATION_ID))
                .isInstanceOf(DataConstraintException.class);

        verify(repository, never()).insert(STUDY_ID, PARTICIPANT_ID, OBSERVATION_ID);
    }

    @Test
    void requestInsertsWhenResyncableAndNotYetPending() {
        givenObservationWithFactory(true);
        when(repository.find(STUDY_ID, PARTICIPANT_ID, OBSERVATION_ID)).thenReturn(Optional.empty());
        when(repository.insert(STUDY_ID, PARTICIPANT_ID, OBSERVATION_ID)).thenReturn(request());

        assertThat(service.requestResync(STUDY_ID, PARTICIPANT_ID, OBSERVATION_ID)).isEqualTo(request());
    }

    @Test
    void isResyncableFollowsTheFactory() {
        Observation observation = observation();
        when(observationService.getObservation(STUDY_ID, OBSERVATION_ID)).thenReturn(Optional.of(observation));
        when(observationService.getObservationFactory(observation)).thenReturn(Optional.of(factory));
        when(factory.isResyncable()).thenReturn(true);

        assertThat(service.isResyncable(STUDY_ID, OBSERVATION_ID)).isTrue();
    }

    @Test
    void isResyncableIsFalseWhenTheFactoryIsMissing() {
        Observation observation = observation();
        when(observationService.getObservation(STUDY_ID, OBSERVATION_ID)).thenReturn(Optional.of(observation));
        when(observationService.getObservationFactory(observation)).thenReturn(Optional.empty());

        assertThat(service.isResyncable(STUDY_ID, OBSERVATION_ID)).isFalse();
    }

    @Test
    void isResyncableFailsWhenObservationIsUnknown() {
        when(observationService.getObservation(STUDY_ID, OBSERVATION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.isResyncable(STUDY_ID, OBSERVATION_ID))
                .isInstanceOf(NotFoundException.class);
    }

    private void givenObservationWithFactory(boolean resyncable) {
        Observation observation = observation();
        when(participantService.getParticipant(STUDY_ID, PARTICIPANT_ID)).thenReturn(participant());
        when(observationService.getObservation(STUDY_ID, OBSERVATION_ID)).thenReturn(Optional.of(observation));
        when(observationService.getObservationFactory(observation)).thenReturn(Optional.of(factory));
        when(factory.isResyncable()).thenReturn(resyncable);
    }

    private static Participant participant() {
        return new Participant().setStudyId(STUDY_ID).setParticipantId(PARTICIPANT_ID);
    }

    private static Observation observation() {
        return new Observation().setStudyId(STUDY_ID).setObservationId(OBSERVATION_ID).setType("lime-survey-observation");
    }

    private static ObservationResyncRequest request() {
        return new ObservationResyncRequest(STUDY_ID, PARTICIPANT_ID, OBSERVATION_ID, Instant.parse("2026-09-14T09:00:00Z"));
    }
}
