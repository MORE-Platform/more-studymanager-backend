package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.ApplicationTest;
import io.redlink.more.studymanager.core.properties.ActionProperties;
import io.redlink.more.studymanager.model.Action;
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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test-containers-flyway")
class InterventionRepositoryTest {
    @Autowired
    private InterventionRepository interventionRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @BeforeEach
    void deleteAll() {
        interventionRepository.clear();
    }

    @Test
    @DisplayName("Interventions are inserted, updated, listed and deleted from database")
    void testInsertListUpdateDelete() {
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

        interventionResponse = interventionRepository.updateIntervention(new Intervention()
                .setStudyId(studyId)
                .setInterventionId(interventionResponse.getInterventionId())
                .setTitle("some new title")
                .setStudyGroupId(studyGroupId)
                .setSchedule("{\"testSchedule\": \"new testValue\"}"));

        assertThat(interventionResponse.getTitle()).isEqualTo("some new title");
        assertThat(interventionResponse.getSchedule()).isEqualTo("{\"testSchedule\": \"new testValue\"}");

        int intervention2Id = interventionRepository.insert(intervention2).getInterventionId();

        assertThat(interventionRepository.listInterventions(studyId).size()).isEqualTo(2);

        interventionRepository.deleteByIds(interventionResponse.getStudyId(), interventionResponse.getInterventionId());

        interventionResponse = interventionRepository.getByIds(intervention2.getStudyId(), intervention2Id);

        assertThat(interventionResponse.getInterventionId()).isEqualTo(intervention2Id);
        assertThat(interventionRepository.listInterventions(studyId).size()).isEqualTo(1);
    }

    @Test
    @DisplayName("Test insert, list and update for actions")
    void testInsertListUpdateForActions() {
        Long studyId = studyRepository.insert(new Study()).getStudyId();
        Integer studyGroupId = studyGroupRepository.insert(new StudyGroup().setStudyId(studyId)).getStudyGroupId();
        Integer interventionId = interventionRepository.insert(new Intervention()
                .setStudyId(studyId).setStudyGroupId(studyGroupId)).getInterventionId();
        Action actionResponse = interventionRepository.createAction(studyId, interventionId, new Action()
                .setType("some-type")
                .setProperties(new ActionProperties(Map.of("property", "value"))));

        assertThat(actionResponse.getActionId()).isEqualTo(1);
        assertThat(actionResponse.getType()).isEqualTo("some-type");
        assertThat(actionResponse.getProperties()).isEqualTo(new ActionProperties(Map.of("property", "value")));
        assertThat(actionResponse.getCreated()).isNotNull();
        assertThat(actionResponse.getModified()).isNotNull();

        interventionRepository.createAction(studyId, interventionId, new Action().setType(""));
        interventionRepository.createAction(studyId, interventionId, new Action().setType(""));
        interventionRepository.createAction(studyId, interventionId, new Action().setType(""));

        assertThat(interventionRepository.listActions(studyId, interventionId).size()).isEqualTo(4);

    }
}
