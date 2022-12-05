package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.StudyGroup;
import java.util.UUID;
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
        assertThat(participantResponse.getModified().getTime()).isLessThan(updated.getModified().getTime());
    }

    @Test
    @DisplayName("Studies are deleted and listed correctly")
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

        assertThat(participantRepository.listParticipants(studyId)).hasSize(3);
        participantRepository.deleteParticipant(studyId, s1.getParticipantId());
        assertThat(participantRepository.listParticipants(studyId)).hasSize(2);
        participantRepository.deleteParticipant(studyId, s2.getParticipantId());
        assertThat(participantRepository.listParticipants(studyId)).hasSize(1);
        participantRepository.deleteParticipant(studyId, s2.getParticipantId());
        assertThat(participantRepository.listParticipants(studyId)).hasSize(1);
        participantRepository.deleteParticipant(studyId, s3.getParticipantId());
        assertThat(participantRepository.listParticipants(studyId)).isEmpty();
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


    @Test
    @DisplayName("Lock active and new Participants")
    void testParticipantLocking() {
        Long studyId = studyRepository.insert(new Study()).getStudyId();

        var activeP = createParticipant(studyId, Participant.Status.ACTIVE);
        var newP = createParticipant(studyId, Participant.Status.NEW);
        var abandonedP = createParticipant(studyId, Participant.Status.ABANDONED);
        var kickedP = createParticipant(studyId, Participant.Status.KICKED_OUT);
        var lockedP = createParticipant(studyId, Participant.Status.LOCKED);

        participantRepository.lockParticipants(studyId);
        assertThat(participantRepository.getByIds(studyId, activeP.getParticipantId()))
                .as("Active -> Locked")
                .hasFieldOrPropertyWithValue("status", Participant.Status.LOCKED);
        assertThat(participantRepository.getByIds(studyId, newP.getParticipantId()))
                .as("New -> Locked")
                .hasFieldOrPropertyWithValue("status", Participant.Status.LOCKED);
        assertThat(participantRepository.getByIds(studyId, abandonedP.getParticipantId()))
                .as("ABANDONED should stay!")
                .hasFieldOrPropertyWithValue("status", Participant.Status.ABANDONED);
        assertThat(participantRepository.getByIds(studyId, kickedP.getParticipantId()))
                .as("KICKED_OUT should stay!")
                .hasFieldOrPropertyWithValue("status", Participant.Status.KICKED_OUT);
        assertThat(participantRepository.getByIds(studyId, lockedP.getParticipantId()))
                .as("LOCKED should stay!")
                .hasFieldOrPropertyWithValue("status", Participant.Status.LOCKED);

    }

    private Participant createParticipant(Long studyId, Participant.Status status) {
        final Participant p = participantRepository.insert(new Participant()
                .setStudyId(studyId)
                .setRegistrationToken(UUID.randomUUID().toString())
        );
        participantRepository.setStatusByIds(studyId, p.getParticipantId(), status);
        final Participant participant = participantRepository.getByIds(studyId, p.getParticipantId());
        assertThat(participant).hasFieldOrPropertyWithValue("status", status);
        return participant;
    }
}
