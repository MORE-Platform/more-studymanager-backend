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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParticipantServiceTest {

    @Mock
    ParticipantRepository participantRepository;

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
}
