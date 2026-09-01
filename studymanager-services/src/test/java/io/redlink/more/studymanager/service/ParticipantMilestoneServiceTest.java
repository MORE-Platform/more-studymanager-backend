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
import io.redlink.more.studymanager.model.Milestone;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.ParticipantMilestone;
import io.redlink.more.studymanager.repository.MilestoneRepository;
import io.redlink.more.studymanager.repository.ParticipantMilestoneRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParticipantMilestoneServiceTest {

    @Mock
    StudyStateService studyStateService;

    @Mock
    ParticipantMilestoneRepository repository;

    @Mock
    MilestoneRepository milestoneRepository;

    @Mock
    ParticipantService participantService;

    @Mock
    ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    ParticipantMilestoneService service;

    private static final Instant DATE_TIME = Instant.parse("2024-06-15T09:00:00Z");

    @Test
    void createFailsWhenAlreadySetForParticipant() {
        when(milestoneRepository.getByIds(1L, 2)).thenReturn(new Milestone().setStudyId(1L).setMilestoneId(2));
        when(participantService.getParticipant(1L, 3))
                .thenReturn(new Participant().setStudyId(1L).setParticipantId(3).setStatus(Participant.Status.ACTIVE));
        when(repository.exists(1L, 3, 2)).thenReturn(true);

        Assertions.assertThrows(DataConstraintException.class,
                () -> service.createParticipantMilestone(1L, 3, 2, DATE_TIME));

        verify(repository, never()).insert(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void createFailsWhenMilestoneDoesNotExist() {
        when(milestoneRepository.getByIds(1L, 2)).thenReturn(null);

        Assertions.assertThrows(NotFoundException.class,
                () -> service.createParticipantMilestone(1L, 3, 2, DATE_TIME));

        verify(repository, never()).insert(any());
    }

    @Test
    void createNotifiesOnlyActiveParticipant() {
        when(milestoneRepository.getByIds(1L, 2)).thenReturn(new Milestone().setStudyId(1L).setMilestoneId(2));
        when(repository.exists(1L, 3, 2)).thenReturn(false);
        when(repository.insert(any(ParticipantMilestone.class))).thenAnswer(i -> i.getArgument(0));

        Participant abandoned = new Participant().setStudyId(1L).setParticipantId(3).setStatus(Participant.Status.ABANDONED);
        when(participantService.getParticipant(1L, 3)).thenReturn(abandoned);

        service.createParticipantMilestone(1L, 3, 2, DATE_TIME);

        verify(applicationEventPublisher, never()).publishEvent(any(ParticipantMilestoneChangedEvent.class));
    }

    @Test
    void createNotifiesActiveParticipant() {
        when(milestoneRepository.getByIds(1L, 2)).thenReturn(new Milestone().setStudyId(1L).setMilestoneId(2));
        when(repository.exists(1L, 3, 2)).thenReturn(false);
        when(repository.insert(any(ParticipantMilestone.class))).thenAnswer(i -> i.getArgument(0));

        Participant active = new Participant().setStudyId(1L).setParticipantId(3).setStatus(Participant.Status.ACTIVE);
        when(participantService.getParticipant(1L, 3)).thenReturn(active);

        service.createParticipantMilestone(1L, 3, 2, DATE_TIME);

        verify(applicationEventPublisher).publishEvent(any(ParticipantMilestoneChangedEvent.class));
    }
}
