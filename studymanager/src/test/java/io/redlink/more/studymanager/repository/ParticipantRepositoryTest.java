package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.Study;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@ActiveProfiles("test-containers-flyway")
class ParticipantRepositoryTest {
    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private StudyRepository studyRepository;

    @BeforeEach
    void deleteAll() {
        participantRepository.clear();
        studyRepository.clear();
    }

    @Test
    @DisplayName("Participant is inserted and returned")
    public void testInsert() {
        Participant participant = new Participant()
                .setAlias("participant x");

        Participant participantResponse = participantRepository.insert(participant);

        assertThat(participantResponse.getStudyId()).isNotNull();
        assertThat(participantResponse.getAlias()).isEqualTo(participant.getAlias());
        assertThat(participantResponse.getStatus()).isEqualTo(Participant.Status.NEW);
    }
    @Test
    @DisplayName("Participant is updated in database and returned")
    public void testUpdate() throws InterruptedException {
        Participant insert = new Participant()
                .setAlias("participant x");

        Participant inserted = participantRepository.insert(insert);

        Participant update = new Participant()
                .setStudyId(inserted.getStudyId())
                .setAlias("new participant x");

        Participant updated = participantRepository.update(update);

        Participant queried = participantRepository.getByIds(inserted.getStudyId(), inserted.getParticipantId());

        assertThat(queried.getAlias()).isEqualTo(updated.getAlias());
        assertThat(queried.getStudyId()).isEqualTo(updated.getStudyId());
        assertThat(queried.getCreated()).isEqualTo(updated.getCreated());

        assertThat(update.getAlias()).isEqualTo(updated.getAlias());
        assertThat(inserted.getStudyId()).isEqualTo(updated.getStudyId());
        assertThat(inserted.getCreated()).isEqualTo(updated.getCreated());
        assertThat(inserted.getModified().getTime()).isLessThan(updated.getModified().getTime());
    }

    @Test
    @DisplayName("Studies are deleted and listed correctly")
    public void testListAndDelete() {
        Long studyId = studyRepository.insert(new Study()).getStudyId();

        Participant s1 = participantRepository.insert(new Participant()
                .setStudyId(studyId));
        Participant s2 = participantRepository.insert(new Participant()
                .setStudyId(studyId));
        Participant s3 = participantRepository.insert(new Participant()
                .setStudyId(studyId));

        Participant participantCompare = new Participant()
                .setStudyId(studyId)
                .setParticipantId(1)
                .setStatus(Participant.Status.NEW);

        assertThat(participantRepository.listParticipants(studyId).size()).isEqualTo(3);
        participantRepository.deleteParticipant(studyId, s1.getParticipantId());
        assertThat(participantRepository.listParticipants(studyId).size()).isEqualTo(2);
        participantRepository.deleteParticipant(studyId, s2.getParticipantId());
        assertThat(participantRepository.listParticipants(studyId).size()).isEqualTo(1);
        participantRepository.deleteParticipant(studyId, s2.getParticipantId());
        assertThat(participantRepository.listParticipants(studyId).size()).isEqualTo(1);
        participantRepository.deleteParticipant(studyId, s3.getParticipantId());
        assertThat(participantRepository.listParticipants(studyId).size()).isEqualTo(0);
        assertThat(participantRepository.insert(new Participant().setStudyId(studyId))).isEqualTo(participantCompare);
    }

    @Test
    @DisplayName("Participant states are set correctly")
    public void testSetState() {
        Long studyId = studyRepository.insert(new Study()).getStudyId();

        Participant participant = participantRepository.insert(new Participant().setStudyId(studyId));
        assertThat(participant.getStatus()).isEqualTo(Participant.Status.NEW);

        participant = participantRepository.getByIds(studyId, participant.getParticipantId());
        assertThat(participant.getStatus()).isEqualTo(Participant.Status.NEW);

        participantRepository.update(new Participant().
                setStudyId(studyId)
                .setParticipantId(participant.getParticipantId())
                .setStatus(Participant.Status.ACCEPTED));

        participant = participantRepository.getByIds(studyId, participant.getParticipantId());
        assertThat(participant.getStatus()).isEqualTo(Participant.Status.ACCEPTED);
    }

}
