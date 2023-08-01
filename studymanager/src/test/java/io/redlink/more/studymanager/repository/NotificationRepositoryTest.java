package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.Contact;
import io.redlink.more.studymanager.model.Notification;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.Study;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test-containers-flyway")
public class NotificationRepositoryTest {

    @Autowired
    NotificationRepository repository;

    @Autowired
    StudyRepository studyRepository;

    @Autowired
    ParticipantRepository participantRepository;

    @Test
    public void testInsert() {
        var s = studyRepository.insert(new Study().setContact(new Contact()));
        var p1 = participantRepository.insert(new Participant().setStudyId(s.getStudyId()).setRegistrationToken("t1"));
        var p2 = participantRepository.insert(new Participant().setStudyId(s.getStudyId()).setRegistrationToken("t2"));

        repository.insert(new Notification()
                .setType(Notification.Type.TEXT)
                .setStudyId(s.getStudyId())
                .setParticipantId(p1.getParticipantId())
                .setMsgId("m1")
                .setData(Map.of("hello", "world"))
        );

        repository.insert(new Notification()
                .setType(Notification.Type.DATA)
                .setStudyId(s.getStudyId())
                .setParticipantId(p2.getParticipantId())
                .setMsgId("m2")
                .setData(Map.of("data", "io"))
        );

        List<Notification> result = repository.listAll();

        Assertions.assertEquals(2, result.size());
    }
}
