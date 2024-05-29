/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.generator.RandomTokenGenerator;
import io.redlink.more.studymanager.repository.ParticipantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParticipantServiceTest {

    @Mock
    ParticipantRepository participantRepository;

    @Mock
    StudyStateService studyStateService;

    @Mock
    ElasticService elasticService;

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
        participantService.deleteParticipant(1L, 1, false);
        verify(elasticService, times(1)).removeDataForParticipant(any(), any());

        participantService.deleteParticipant(1L, 1, true);
        verify(elasticService, times(2)).removeDataForParticipant(any(), any());

    }
}
