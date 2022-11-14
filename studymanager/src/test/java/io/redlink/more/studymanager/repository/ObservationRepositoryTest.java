package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.ApplicationTest;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.StudyGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test-containers-flyway")
class ObservationRepositoryTest {
    @Autowired
    private ObservationRepository observationRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @BeforeEach
    void deleteAll() {
        observationRepository.clear();
    }

    @Test
    @DisplayName("Observations are inserted, updated, listed and deleted from database")
    public void testInsertListUpdateDelete() {
        String type = "accelerometer";
        Long studyId = studyRepository.insert(new Study()).getStudyId();
        Integer studyGroupId = studyGroupRepository.insert(new StudyGroup().setStudyId(studyId)).getStudyGroupId();


        Observation observation = new Observation()
                .setStudyId(studyId)
                .setType(type)
                .setTitle("some title")
                .setStudyGroupId(studyGroupId)
                .setProperties(new ObservationProperties(Map.of("testProperty", "testValue")));

        Observation observationResponse = observationRepository.insert(observation);

        assertThat(observationResponse.getObservationId()).isNotNull();
        assertThat(observationResponse.getTitle()).isEqualTo(observation.getTitle());
        assertThat(observationResponse.getProperties()).isEqualTo(observation.getProperties());

        Integer oldId = observationResponse.getObservationId();

        observationResponse.setType("new type")
                .setTitle("some new title")
                .setSchedule("{\"testSchedule\": \"testTime\"}");

        Observation compareObservationResponse = observationRepository.updateObservation(observationResponse);

        assertThat(compareObservationResponse.getTitle()).isEqualTo(observationResponse.getTitle());
        assertThat(compareObservationResponse.getType()).isEqualTo(type);
        assertThat(compareObservationResponse.getObservationId()).isEqualTo(oldId);
        assertThat(compareObservationResponse.getSchedule()).isNotEqualTo(observation.getSchedule());

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
