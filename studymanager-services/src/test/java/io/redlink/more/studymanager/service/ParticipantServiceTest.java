/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.event.StudyStateChangedEvent;
import io.redlink.more.studymanager.model.LoginTokenApplication;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.generator.RandomTokenGenerator;
import io.redlink.more.studymanager.repository.ParticipantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipantServiceTest {

    @Mock
    ParticipantRepository participantRepository;

    @Mock
    StudyStateService studyStateService;

    @Mock
    ElasticService elasticService;

    @Mock
    LoginTokenService loginTokenService;

    @InjectMocks
    ParticipantService participantService;


    @Test
    @DisplayName("When the participant is saved it should return the participant with id.")
    void testSaveStudy() {
        String token = RandomTokenGenerator.generate();

        Participant participant = new Participant()
                .setStudyId(1L)
                .setAlias("participant x")
                .setRegistrationToken(token);

        when(participantRepository.insert(any(Participant.class)))
                .thenReturn(new Participant().setParticipantId(1).setStudyId(1L).setAlias("participant x").setRegistrationToken(token));

        Participant participantResponse = participantService.createParticipant(participant);

        assertThat(participantResponse.getStudyId()).isEqualTo(1L);
        assertThat(participantResponse.getParticipantId()).isEqualTo(1);
        assertThat(participantResponse.getAlias()).isSameAs(participant.getAlias());
    }

    @Test
    void testDeleteParticipant() {
        Participant participant = new Participant()
                .setParticipantId(1);

        participantService.deleteParticipant(1L, 1, true);
        verify(loginTokenService, times(1)).deleteParticipantTokens(1L, 1);
        participantService.deleteParticipant(1L, 1, false);
        verify(loginTokenService, times(2)).deleteParticipantTokens(1L, 1);
        verify(elasticService, times(1)).removeDataForParticipant(any(), any());

        participantService.deleteParticipant(1L, 1, true);
        verify(elasticService, times(2)).removeDataForParticipant(any(), any());
        verify(loginTokenService, times(3)).deleteParticipantTokens(1L, 1);
    }

    @Test
    void testHandleStudyStateChange() {
        Study study = new Study()
                .setStudyId(1L)
                .setTitle("Test study")
                .setStudyState(Study.Status.ACTIVE)
                .setParticipantPortalAccess(true);

        participantService.handleStudyStateChange(new StudyStateChangedEvent(this,study, Study.Status.DRAFT ));
        Mockito.verify(participantRepository, Mockito.never()).resetParticipants(anyLong(), any());
        Mockito.verify(participantRepository, Mockito.never()).cleanupParticipants(anyLong());
        Mockito.verify(loginTokenService, Mockito.times(1)).createMissingTokens(eq(1L), eq(LoginTokenApplication.PARTICIPANT_PORTAL.name()));

        Mockito.reset(loginTokenService);
        study.setParticipantPortalAccess(false);
        participantService.handleStudyStateChange(new StudyStateChangedEvent(this, study, Study.Status.DRAFT));
        Mockito.verify(loginTokenService, Mockito.times(1)).deleteTokens(eq(1L), eq(LoginTokenApplication.PARTICIPANT_PORTAL.name()));

        //validate participants are reset if study goes to DRAFT state
        study.setStudyState(Study.Status.DRAFT);
        participantService.handleStudyStateChange(new StudyStateChangedEvent(this,study, Study.Status.PREVIEW ));
        Mockito.verify(participantRepository, Mockito.times(1)).resetParticipants(eq(study.getStudyId()), any());
        Mockito.verify(participantRepository, Mockito.never()).cleanupParticipants(anyLong());
        Mockito.verify(loginTokenService, Mockito.times(1)).deleteStudyTokens(eq(study.getStudyId()));

        Mockito.reset(participantRepository, loginTokenService);

        //validate participants are cleaned if study goes to CLOSED state
        study.setStudyState(Study.Status.CLOSED);
        participantService.handleStudyStateChange(new StudyStateChangedEvent(this,study, Study.Status.ACTIVE ));
        Mockito.verify(participantRepository, Mockito.times(1)).cleanupParticipants(eq(study.getStudyId()));
        Mockito.verify(participantRepository, Mockito.never()).resetParticipants(anyLong(), any());
        Mockito.verify(loginTokenService, Mockito.times(1)).deleteStudyTokens(eq(study.getStudyId()));

    }

    @Test
    void testSetStatusCleanup() {
        participantService.setStatus(1L, 1, Participant.Status.KICKED_OUT);
        verify(participantRepository).cleanupParticipant(1L, 1);
        verify(loginTokenService).deleteParticipantTokens(1L, 1);

        Mockito.reset(participantRepository, loginTokenService);
        participantService.setStatus(1L, 1, Participant.Status.ACTIVE);
        verify(participantRepository, times(0)).cleanupParticipant(anyLong(), any());
        verify(loginTokenService, times(0)).deleteParticipantTokens(anyLong(), any());
    }
}
