/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.configuration.JPAConfiguration;
import io.redlink.more.studymanager.core.properties.ActionProperties;
import io.redlink.more.studymanager.core.properties.TriggerProperties;
import io.redlink.more.studymanager.model.*;
import io.redlink.more.studymanager.model.scheduler.Event;
import io.redlink.more.studymanager.model.scheduler.RecurrenceRule;
import io.redlink.more.studymanager.utils.MapperUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@EnableAutoConfiguration
@ContextConfiguration(classes = {
    InterventionRepository.class, StudyRepository.class, StudyGroupRepository.class, ObservationGroupRepository.class,
    JPAConfiguration.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test-containers-flyway")
class InterventionRepositoryTest {

    @Autowired
    private InterventionRepository interventionRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @Autowired
    private ObservationGroupRepository observationGroupRepository;

    @BeforeEach
    void deleteAll() {
        interventionRepository.clear();
    }

    @Test
    @DisplayName("Interventions are inserted, updated, listed and deleted from database")
    void testInsertListUpdateDelete() {
        Long studyId = studyRepository.insert(new Study().setContact(new Contact().setPerson("test").setEmail("test"))).getStudyId();
        Integer studyGroupId = studyGroupRepository.insert(new StudyGroup().setStudyId(studyId)).getStudyGroupId();
        Instant startTime = Instant.now();
        Instant endTime = Instant.now().plus(2, ChronoUnit.HOURS);

        Integer observationGroupId1 = observationGroupRepository.insert(new ObservationGroup().setStudyId(studyId).setTitle("Observation Group 1").setPurpose("test")).getObservationGroupId();
        Integer observationGroupId2 = observationGroupRepository.insert(new ObservationGroup().setStudyId(studyId).setTitle("Observation Group 2").setPurpose("test")).getObservationGroupId();

        Intervention intervention = new Intervention()
                .setStudyId(studyId)
                .setTitle("some title")
                .setStudyGroupId(studyGroupId)
                .setSchedule(new Event().setDateEnd(Instant.now()).setDateEnd(Instant.now().plusSeconds(60)))
                .setSchedule(new Event()
                        .setDateStart(startTime)
                        .setDateEnd(endTime)
                        .setRRule(new RecurrenceRule().setFreq("DAILY").setCount(7)))
                .setObservationGroupId(observationGroupId1);

        //insert
        Intervention interventionResponse = interventionRepository.insert(intervention);

        assertThat(interventionResponse.getInterventionId()).isNotNull();
        assertThat(interventionResponse.getTitle()).isEqualTo(intervention.getTitle());
        assertThat(((Event)interventionResponse.getSchedule()).getDateStart()).isEqualTo(startTime);
        assertThat(MapperUtils.writeValueAsString(interventionResponse.getSchedule()))
                .isEqualTo(MapperUtils.writeValueAsString(intervention.getSchedule()));
        assertThat(interventionResponse.getObservationGroupId()).isEqualTo(observationGroupId1);

        //update
        interventionResponse = interventionRepository.updateIntervention(interventionResponse
                .setTitle("some new title")
                .setObservationGroupId(null));

        assertThat(interventionResponse.getTitle()).isEqualTo("some new title");
        assertThat(interventionResponse.getObservationGroupId()).isNull();

        Intervention intervention2 = interventionRepository.insert(new Intervention()
                .setStudyId(studyId)
                .setTitle("Intervention 2")
                .setSchedule(new Event().setDateEnd(Instant.now()).setDateEnd(Instant.now().plusSeconds(60)))
                .setStudyGroupId(null)
                .setObservationGroupId(null));
        Intervention intervention3a = interventionRepository.insert(new Intervention()
                .setStudyId(studyId)
                .setTitle("Intervaion 3a")
                .setSchedule(new Event().setDateEnd(Instant.now()).setDateEnd(Instant.now().plusSeconds(60)))
                .setStudyGroupId(null)
                .setObservationGroupId(observationGroupId1));
        Intervention intervention3b = interventionRepository.insert(new Intervention()
                .setStudyId(studyId)
                .setTitle("Intervaion 3b")
                .setSchedule(new Event().setDateEnd(Instant.now()).setDateEnd(Instant.now().plusSeconds(60)))
                .setStudyGroupId(null)
                .setObservationGroupId(observationGroupId2));
        Intervention intervention4a = interventionRepository.insert(new Intervention()
                .setStudyId(studyId)
                .setTitle("Intervaion 4a")
                .setSchedule(new Event().setDateEnd(Instant.now()).setDateEnd(Instant.now().plusSeconds(60)))
                .setStudyGroupId(studyGroupId)
                .setObservationGroupId(observationGroupId1));
        Intervention intervention4b = interventionRepository.insert(new Intervention()
                .setStudyId(studyId)
                .setTitle("Intervaion 4b")
                .setSchedule(new Event().setDateEnd(Instant.now()).setDateEnd(Instant.now().plusSeconds(60)))
                .setStudyGroupId(studyGroupId)
                .setObservationGroupId(observationGroupId2));

        assertThat(interventionRepository.listInterventions(studyId).size()).isEqualTo(6);

        //list interventions with no groups
        assertThat(interventionRepository.listInterventionsForGroup(studyId,null, Set.of()))
                .extracting(Intervention::getInterventionId)
                .containsExactly(intervention2.getInterventionId()); //intervantion 2 is not part of any group
        //same for none existing group IDs
        assertThat(interventionRepository.listInterventionsForGroup(studyId,-1, Set.of(-1)))
                .extracting(Intervention::getInterventionId)
                .containsExactly(intervention2.getInterventionId()); //intervantion 2 is not part of any group
        //list interventions in observation group 1 with no studyGroup (or no studyGroup)
        assertThat(interventionRepository.listInterventionsForGroup(studyId,null, Set.of(observationGroupId1)))
                .extracting(Intervention::getInterventionId)
                .containsExactly(intervention2.getInterventionId(), intervention3a.getInterventionId());
        //same for none existing group
        assertThat(interventionRepository.listInterventionsForGroup(studyId,-1, Set.of(observationGroupId1)))
                .extracting(Intervention::getInterventionId)
                .containsExactly(intervention2.getInterventionId(), intervention3a.getInterventionId());
        //list interventions in study group 1 and observation group 1 with no studyGroup (or no studyGroup)
        assertThat(interventionRepository.listInterventionsForGroup(studyId, studyGroupId, Set.of(observationGroupId1)))
                .extracting(Intervention::getInterventionId)
                .containsExactly(
                        interventionResponse.getInterventionId(), intervention2.getInterventionId(),
                        intervention3a.getInterventionId(), intervention4a.getInterventionId());

        //list interventions in study group 1 and observation group 1 or observation group 2with no studyGroup (or no studyGroup)
        assertThat(interventionRepository.listInterventionsForGroup(studyId, studyGroupId, Set.of(observationGroupId1, observationGroupId2)))
                .extracting(Intervention::getInterventionId)
                .containsExactly(
                        interventionResponse.getInterventionId(), intervention2.getInterventionId(),
                        intervention3a.getInterventionId(), intervention3b.getInterventionId(),
                        intervention4a.getInterventionId(), intervention4b.getInterventionId());

        interventionRepository.deleteByIds(interventionResponse.getStudyId(), interventionResponse.getInterventionId());

        interventionResponse = interventionRepository.getByIds(intervention2.getStudyId(), intervention2.getInterventionId());

        assertThat(interventionResponse.getInterventionId()).isEqualTo(intervention2.getInterventionId());
        assertThat(interventionRepository.listInterventions(studyId).size()).isEqualTo(5);

        //assert delete Observation Group sets property of Observation to null
        observationGroupRepository.deleteById(studyId, observationGroupId1);
        assertThat((interventionRepository.getByIds(studyId, intervention3a.getInterventionId()).getObservationGroupId()))
                .isNull();
        assertThat((interventionRepository.getByIds(studyId, intervention4a.getInterventionId()).getObservationGroupId()))
                .isNull();


    }

    @Test
    @DisplayName("Test insert, list and update for actions")
    void testInsertListUpdateForActions() {
        Long studyId = studyRepository.insert(new Study().setContact(new Contact().setPerson("test").setEmail("test"))).getStudyId();
        Integer studyGroupId = studyGroupRepository.insert(new StudyGroup().setStudyId(studyId)).getStudyGroupId();
        Integer interventionId = interventionRepository.insert(new Intervention()
                .setStudyId(studyId).setStudyGroupId(studyGroupId)).getInterventionId();
        Action actionResponse1 = interventionRepository.createAction(studyId, interventionId, new Action()
                .setType("some-type")
                .setProperties(new ActionProperties(Map.of("property", "value"))));

        assertThat(actionResponse1.getActionId()).isEqualTo(1);
        assertThat(actionResponse1.getType()).isEqualTo("some-type");
        assertThat(actionResponse1.getProperties()).isEqualTo(new ActionProperties(Map.of("property", "value")));
        assertThat(actionResponse1.getCreated()).isNotNull();
        assertThat(actionResponse1.getModified()).isNotNull();

        Action actionResponse2 = interventionRepository.createAction(studyId, interventionId, new Action().setType(""));
        Action actionResponse3 = interventionRepository.createAction(studyId, interventionId, new Action().setType(""));
        Action actionResponse4 = interventionRepository.createAction(studyId, interventionId, new Action().setType(""));

        assertThat(interventionRepository.listActions(studyId, interventionId).size()).isEqualTo(4);

        actionResponse1 = interventionRepository.updateAction(studyId, interventionId, actionResponse1.getActionId(),
                new Action().setProperties(new ActionProperties(Map.of("property", "new value"))));

        assertThat(actionResponse1.getActionId()).isEqualTo(1);
        assertThat(actionResponse1.getProperties()).isEqualTo(new ActionProperties(Map.of("property", "new value")));

        interventionRepository.deleteActionByIds(studyId, interventionId, actionResponse1.getActionId());
        assertThat(interventionRepository.listActions(studyId, interventionId).size()).isEqualTo(3);
        interventionRepository.deleteActionByIds(studyId, interventionId, actionResponse2.getActionId());
        assertThat(interventionRepository.listActions(studyId, interventionId).size()).isEqualTo(2);
        interventionRepository.deleteActionByIds(studyId, interventionId, actionResponse3.getActionId());
        assertThat(interventionRepository.listActions(studyId, interventionId).size()).isEqualTo(1);
        interventionRepository.deleteActionByIds(studyId, interventionId, actionResponse4.getActionId());
        assertThat(interventionRepository.listActions(studyId, interventionId).size()).isEqualTo(0);

    }

    @Test
    @DisplayName("Triggers can be updated")
    public void testUpdateTrigger() {
        Long studyId = studyRepository.insert(new Study().setContact(new Contact().setPerson("test").setEmail("test"))).getStudyId();
        Integer studyGroupId = studyGroupRepository.insert(new StudyGroup().setStudyId(studyId)).getStudyGroupId();
        Integer interventionId = interventionRepository.insert(new Intervention().setStudyId(studyId)
                        .setStudyGroupId(studyGroupId)).getInterventionId();

        Trigger trigger = new Trigger()
                .setType("my-type")
                .setProperties(new TriggerProperties(Map.of("property", "value")));

        Trigger triggerResponse = interventionRepository.updateTrigger(studyId, interventionId, trigger);

        assertThat(triggerResponse.getType()).isEqualTo(trigger.getType());
        assertThat(triggerResponse.getProperties()).isEqualTo(trigger.getProperties());
        assertThat(triggerResponse.getCreated()).isNotNull();
        assertThat(triggerResponse.getModified()).isNotNull();
    }
}
