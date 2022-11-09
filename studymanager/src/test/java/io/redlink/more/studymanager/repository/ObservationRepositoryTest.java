package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.ApplicationTest;
import io.redlink.more.studymanager.model.Observation;
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
class ObservationRepositoryTest extends ApplicationTest {
    @Autowired
    private ObservationRepository observationRepository;

    @Autowired
    private StudyRepository studyRepository;

    @BeforeEach
    void deleteAll() {
        observationRepository.clear();
        studyRepository.clear();
    }

    @Test
    @DisplayName("Observation is inserted in database and returned")
    public void testInsertListUpdateDelete() {
        Long studyId = studyRepository.insert(new Study()).getStudyId();

        Observation observation = new Observation()
                .setStudyId(studyId)
                .setType("accelerometer")
                .setTitle("some title");

        Observation observationResponse = observationRepository.insert(observation);

        assertThat(observationResponse.getObservationId()).isNotNull();
        assertThat(observationResponse.getTitle()).isEqualTo(observation.getTitle());

        Observation observationResponse2 = observationRepository.insert(new Observation()
                .setStudyId(studyId)
                .setType("gps")
                .setType("new Title"));

        assertThat((observationRepository.listObservations(studyId)).size()).isEqualTo(2);
        observationRepository.deleteObservation(studyId, observationResponse.getObservationId());
        assertThat((observationRepository.listObservations(studyId)).size()).isEqualTo(1);
        observationRepository.deleteObservation(studyId, observationResponse2.getObservationId());
        assertThat((observationRepository.listObservations(studyId)).size()).isEqualTo(0);
    }
}
