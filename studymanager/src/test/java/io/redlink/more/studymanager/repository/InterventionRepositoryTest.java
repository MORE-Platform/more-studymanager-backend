package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.ApplicationTest;
import io.redlink.more.studymanager.model.Intervention;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.StudyGroup;
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
class InterventionRepositoryTest extends ApplicationTest {
    @Autowired
    private InterventionRepository interventionRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @BeforeEach
    void deleteAll() {
        // interventionRepository.clear();
    }

    @Test
    @DisplayName("Interventions are inserted, updated, listed and deleted from database")
    public void testInsertListUpdateDelete() {
        Long studyId = studyRepository.insert(new Study()).getStudyId();
        Integer studyGroupId = studyGroupRepository.insert(new StudyGroup().setStudyId(studyId)).getStudyGroupId();


        Intervention intervention = new Intervention()
                .setStudyId(studyId)
                .setTitle("some title")
                .setStudyGroupId(studyGroupId)
                .setSchedule("{\"testSchedule\": \"testValue\"}");

        Intervention intervention2 = new Intervention()
                .setStudyId(studyId)
                .setTitle("some other title")
                .setStudyGroupId(studyGroupId)
                .setSchedule("{\"testSchedule\": \"testValue\"}");

        Intervention interventionResponse = interventionRepository.insert(intervention);

        assertThat(interventionResponse.getInterventionId()).isNotNull();
        assertThat(interventionResponse.getTitle()).isEqualTo(intervention.getTitle());
        assertThat(interventionResponse.getSchedule()).isEqualTo(intervention.getSchedule());

        interventionRepository.insert(intervention2);

        assertThat(interventionRepository.listInterventions(studyId).size()).isEqualTo(2);
    }
}
