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
import io.redlink.more.studymanager.repository.MilestoneRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MilestoneServiceTest {

    @Mock
    StudyStateService studyStateService;

    @Mock
    MilestoneRepository repository;

    @InjectMocks
    MilestoneService milestoneService;

    @Test
    void deleteMilestoneDecrementsTrailingOrderIndices() {
        when(repository.getByIds(1L, 2))
                .thenReturn(new Milestone().setStudyId(1L).setMilestoneId(2).setName("M2").setOrderIndex(3));
        when(repository.countActiveParticipantMilestones(1L, 2)).thenReturn(0);

        milestoneService.deleteMilestone(1L, 2);

        verify(repository).deleteById(1L, 2);
        verify(repository).decrementOrderIndexAbove(1L, 3);
    }

    @Test
    void deleteMilestoneFailsWhenActiveParticipantHasItSet() {
        when(repository.getByIds(1L, 2))
                .thenReturn(new Milestone().setStudyId(1L).setMilestoneId(2).setName("M2").setOrderIndex(3));
        when(repository.countActiveParticipantMilestones(1L, 2)).thenReturn(1);

        Assertions.assertThrows(DataConstraintException.class, () -> milestoneService.deleteMilestone(1L, 2));

        verify(repository, never()).deleteById(anyLong(), anyInt());
        verify(repository, never()).decrementOrderIndexAbove(anyLong(), anyInt());
    }

    @Test
    void deleteMilestoneFailsWhenMilestoneDoesNotExist() {
        when(repository.getByIds(1L, 99)).thenReturn(null);

        Assertions.assertThrows(NotFoundException.class, () -> milestoneService.deleteMilestone(1L, 99));
    }

    @Test
    void getMilestoneFailsWhenNotFound() {
        when(repository.getByIds(1L, 99)).thenReturn(null);

        Assertions.assertThrows(NotFoundException.class, () -> milestoneService.getMilestone(1L, 99));
    }

    @Test
    void addMilestoneOnlyNeedsAName() {
        when(repository.insert(any(Milestone.class)))
                .thenAnswer(i -> ((Milestone) i.getArgument(0)).setMilestoneId(1).setOrderIndex(1));

        Milestone created = milestoneService.addMilestone(1L, "Baseline");

        assertThat(created.getName()).isEqualTo("Baseline");
        assertThat(created.getMilestoneId()).isEqualTo(1);

        ArgumentCaptor<Milestone> captor = ArgumentCaptor.forClass(Milestone.class);
        verify(repository).insert(captor.capture());
        assertThat(captor.getValue().getStudyId()).isEqualTo(1L);
        assertThat(captor.getValue().getName()).isEqualTo("Baseline");
    }

    @Test
    void updateMilestoneWithUnchangedOrderIndexDoesNotReorder() {
        when(repository.getByIds(1L, 2))
                .thenReturn(new Milestone().setStudyId(1L).setMilestoneId(2).setName("M2").setOrderIndex(2));

        milestoneService.updateMilestone(1L, 2, "Renamed", 2);

        ArgumentCaptor<Milestone> captor = ArgumentCaptor.forClass(Milestone.class);
        verify(repository).update(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Renamed");
        verify(repository, never()).shiftOrderIndexRange(anyLong(), anyInt(), anyInt(), anyInt());
        verify(repository, never()).setOrderIndex(anyLong(), anyInt(), anyInt());
    }

    @Test
    void updateMilestoneMovingLaterShiftsInBetweenMilestonesUp() {
        when(repository.getByIds(1L, 2))
                .thenReturn(new Milestone().setStudyId(1L).setMilestoneId(2).setName("M2").setOrderIndex(2));
        when(repository.countByStudyId(1L)).thenReturn(5);

        milestoneService.updateMilestone(1L, 2, "M2", 4);

        verify(repository).shiftOrderIndexRange(1L, 3, 4, -1);
        verify(repository).setOrderIndex(1L, 2, 4);
    }

    @Test
    void updateMilestoneMovingEarlierShiftsInBetweenMilestonesDown() {
        when(repository.getByIds(1L, 2))
                .thenReturn(new Milestone().setStudyId(1L).setMilestoneId(2).setName("M2").setOrderIndex(4));
        when(repository.countByStudyId(1L)).thenReturn(5);

        milestoneService.updateMilestone(1L, 2, "M2", 1);

        verify(repository).shiftOrderIndexRange(1L, 1, 3, 1);
        verify(repository).setOrderIndex(1L, 2, 1);
    }

    @Test
    void updateMilestoneClampsRequestedOrderIndexToValidRange() {
        when(repository.getByIds(1L, 2))
                .thenReturn(new Milestone().setStudyId(1L).setMilestoneId(2).setName("M2").setOrderIndex(2));
        when(repository.countByStudyId(1L)).thenReturn(3);

        milestoneService.updateMilestone(1L, 2, "M2", 99);

        verify(repository).shiftOrderIndexRange(1L, 3, 3, -1);
        verify(repository).setOrderIndex(1L, 2, 3);
    }
}
