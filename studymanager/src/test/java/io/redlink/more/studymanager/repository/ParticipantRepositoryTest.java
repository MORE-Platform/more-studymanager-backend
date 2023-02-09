package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.StudyGroup;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test-containers-flyway")
class ParticipantRepositoryTest {
    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @BeforeEach
    void deleteAll() {
        participantRepository.clear();
    }

    @Test
    @DisplayName("Participant is inserted and returned")
    void testInsert() {
        Long studyId = studyRepository.insert(new Study()).getStudyId();
        Integer studyGroupId = studyGroupRepository.insert(new StudyGroup()
                .setStudyId(studyId)).getStudyGroupId();

        Participant participant = new Participant()
                .setAlias("participant x")
                .setStudyGroupId(studyGroupId)
                .setStudyId(studyId)
                .setRegistrationToken("TEST123");

        Participant participantResponse = participantRepository.insert(participant);

        assertThat(participantResponse.getAlias()).isEqualTo(participant.getAlias());
        assertThat(participantResponse.getStatus()).isEqualTo(Participant.Status.NEW);
        assertThat(participantResponse.getParticipantId()).isNotNull();

        Participant update = participantResponse.setAlias("new participant x");

        Participant updated = participantRepository.update(update);

        Participant queried = participantRepository.getByIds(participantResponse.getStudyId(), participantResponse.getParticipantId());

        assertThat(queried.getAlias()).isEqualTo(updated.getAlias());
        assertThat(queried.getStudyId()).isEqualTo(updated.getStudyId());
        assertThat(queried.getCreated()).isEqualTo(updated.getCreated());
        assertThat(queried.getStatus()).isEqualTo(updated.getStatus());

        assertThat(update.getAlias()).isEqualTo(updated.getAlias());
        assertThat(participantResponse.getStudyId()).isEqualTo(updated.getStudyId());
        assertThat(participantResponse.getCreated()).isEqualTo(updated.getCreated());
        assertThat(participantResponse.getModified().toEpochMilli()).isLessThan(updated.getModified().toEpochMilli());
    }

    @Test
    @DisplayName("Participants are deleted and listed correctly")
    void testListAndDelete() {
        Long studyId = studyRepository.insert(new Study()).getStudyId();

        Participant s1 = participantRepository.insert(new Participant()
                .setStudyId(studyId)
                .setRegistrationToken("TEST123"));
        Participant s2 = participantRepository.insert(new Participant()
                .setStudyId(studyId)
                .setRegistrationToken("TEST456"));
        Participant s3 = participantRepository.insert(new Participant()
                .setStudyId(studyId)
                .setRegistrationToken("TEST789"));

        assertThat(participantRepository.listParticipants(studyId))
                .hasSize(3);
        assertThat(participantRepository.deleteParticipant(studyId, s1.getParticipantId()))
                .isEmpty();
        assertThat(participantRepository.listParticipants(studyId))
                .hasSize(2);
        assertThat(participantRepository.deleteParticipant(studyId, s2.getParticipantId()))
                .isEmpty();
        assertThat(participantRepository.listParticipants(studyId))
                .hasSize(1);
        assertThat(participantRepository.deleteParticipant(studyId, s2.getParticipantId()))
                .isEmpty();
        assertThat(participantRepository.listParticipants(studyId))
                .hasSize(1);
        assertThat(participantRepository.deleteParticipant(studyId, s3.getParticipantId()))
                .isEmpty();
        assertThat(participantRepository.listParticipants(studyId))
                .isEmpty();

        var p4 = createParticipant(studyId, Participant.Status.ACTIVE);
        var p5 = createParticipant(studyId);
        var p6 = createParticipant(studyId, Participant.Status.ABANDONED);
        var p7 = createParticipant(studyId, Participant.Status.KICKED_OUT);
        assertThat(participantRepository.listParticipants(studyId))
                .hasSize(4);
        assertThat(participantRepository.deleteParticipant(studyId, p4.getParticipantId()))
                .isPresent()
                .hasValueSatisfying(p -> assertThat(p).hasFieldOrPropertyWithValue("status", Participant.Status.KICKED_OUT));
        assertThat(participantRepository.listParticipants(studyId))
                .hasSize(4);
        assertThat(participantRepository.deleteParticipant(studyId, p5.getParticipantId()))
                .isEmpty();
        assertThat(participantRepository.listParticipants(studyId))
                .hasSize(3);
        assertThat(participantRepository.deleteParticipant(studyId, p6.getParticipantId()))
                .isPresent()
                .hasValueSatisfying(p -> assertThat(p).hasFieldOrPropertyWithValue("status", Participant.Status.ABANDONED));
        assertThat(participantRepository.listParticipants(studyId))
                .hasSize(3);
        assertThat(participantRepository.deleteParticipant(studyId, p7.getParticipantId()))
                .isPresent()
                .hasValueSatisfying(p -> assertThat(p).hasFieldOrPropertyWithValue("status", Participant.Status.KICKED_OUT));
        assertThat(participantRepository.listParticipants(studyId))
                .hasSize(3);

    }

    private Participant createParticipant(Long studyId, Participant.Status status) {
        final Participant p = createParticipant(studyId);
        if (status != null) {
            return participantRepository.setStatusByIds(p.getStudyId(), p.getParticipantId(), status)
                    .orElseThrow();
        }
        return p;

    }

    private Participant createParticipant(Long studyId) {
        return participantRepository.insert(new Participant()
                .setStudyId(studyId)
                .setRegistrationToken(RandomStringUtils.randomAlphanumeric(12))
        );
    }

    @Test
    @DisplayName("Participant states are set correctly")
    void testSetState() {
        Long studyId = studyRepository.insert(new Study()).getStudyId();

        Participant participant = participantRepository.insert(new Participant().setStudyId(studyId).setRegistrationToken("TEST123"));
        assertThat(participant.getStatus()).isEqualTo(Participant.Status.NEW);

        participant = participantRepository.getByIds(studyId, participant.getParticipantId());
        assertThat(participant.getStatus()).isEqualTo(Participant.Status.NEW);

        participantRepository.setStatusByIds(studyId, participant.getParticipantId(), Participant.Status.ACTIVE);

        participant = participantRepository.getByIds(studyId, participant.getParticipantId());
        assertThat(participant.getStatus()).isEqualTo(Participant.Status.ACTIVE);
    }

    @Test
    @DisplayName("Participants study group must be undefined")
    void testUndefinedStudyGroup() {
        Long studyId = studyRepository.insert(new Study()).getStudyId();
        Participant participant = participantRepository
                .insert(new Participant().setStudyId(studyId).setRegistrationToken("abc"));
        assertThat(participant.getStudyGroupId()).isNull();
    }

}
