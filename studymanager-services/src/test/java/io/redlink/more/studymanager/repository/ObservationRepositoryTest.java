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
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.model.*;
import io.redlink.more.studymanager.model.scheduler.*;
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
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@EnableAutoConfiguration
@ContextConfiguration(classes = {
        ObservationRepository.class, StudyRepository.class, ParticipantRepository.class, ObservationGroupRepository.class,
        StudyGroupRepository.class, JPAConfiguration.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test-containers-flyway")
class ObservationRepositoryTest {

    @Autowired
    private ObservationRepository observationRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @Autowired
    private ObservationGroupRepository observationGroupRepository;

    @BeforeEach
    void deleteAll() {
        observationRepository.clear();
    }

    @Test
    @DisplayName("Observations are inserted, updated, listed and deleted from database")
    public void testInsertListUpdateDelete() {
        String type = "accelerometer";
        Long studyId = studyRepository.insert(new Study().setContact(new Contact().setPerson("test").setEmail("test"))).getStudyId();
        Integer studyGroupId = studyGroupRepository.insert(new StudyGroup().setStudyId(studyId)).getStudyGroupId();
        Instant startTime = Instant.now();
        Instant endTime = Instant.now().plus(2, ChronoUnit.HOURS);

        Integer observationGroupId1 = observationGroupRepository.insert(new ObservationGroup().setStudyId(studyId).setTitle("Observation Group 1").setPurpose("test")).getObservationGroupId();
        Integer observationGroupId2 = observationGroupRepository.insert(new ObservationGroup().setStudyId(studyId).setTitle("Observation Group 2").setPurpose("test")).getObservationGroupId();

        Observation observation = new Observation()
                .setStudyId(studyId)
                .setType(type)
                .setTitle("some title")
                .setStudyGroupId(studyGroupId)
                .setProperties(new ObservationProperties(Map.of("testProperty", "testValue")))
                .setSchedule(new Event()
                        .setDateStart(startTime)
                        .setDateEnd(endTime)
                        .setRRule(new RecurrenceRule().setFreq("DAILY").setCount(7)))
                .setHidden(true)
                .setNoSchedule(false)
                .setObservationGroupId(observationGroupId1);

        Observation observationResponse = observationRepository.insert(observation);

        assertThat(observationResponse.getObservationId()).isNotNull();
        assertThat(observationResponse.getTitle()).isEqualTo(observation.getTitle());
        assertThat(observationResponse.getProperties()).isEqualTo(observation.getProperties());
        assertThat(MapperUtils.writeValueAsString(observationResponse.getSchedule()))
                .isEqualTo(MapperUtils.writeValueAsString(observation.getSchedule()));
        assertThat(observationResponse.getObservationGroupId()).isEqualTo(observationGroupId1);

        Integer oldId = observationResponse.getObservationId();

        observationResponse.setType("new type")
                .setTitle("some new title")
                .setSchedule(new Event().setDateEnd(Instant.now()).setDateEnd(Instant.now().plusSeconds(60)))
                .setObservationGroupId(null);

        Observation compareObservationResponse = observationRepository.updateObservation(observationResponse);

        assertThat(compareObservationResponse.getTitle()).isEqualTo(observationResponse.getTitle());
        assertThat(compareObservationResponse.getType()).isEqualTo(type);
        assertThat(compareObservationResponse.getObservationId()).isEqualTo(oldId);
        assertThat(compareObservationResponse.getSchedule()).isNotEqualTo(observation.getSchedule());
        assertThat(observationResponse.getObservationGroupId()).isNull();

        Observation observationResponse2 = observationRepository.insert(new Observation()
                .setStudyId(studyId)
                .setType("gps")
                .setType("new Title")
                .setHidden(true)
                .setNoSchedule(true)
        );
        Observation observationResponse3a = observationRepository.insert(new Observation()
                .setStudyId(studyId)
                .setType("gps")
                .setType("new Title - obs-group 1")
                .setHidden(true)
                .setNoSchedule(true)
                .setObservationGroupId(observationGroupId1)
        );
        Observation observationResponse3b = observationRepository.insert(new Observation()
                .setStudyId(studyId)
                .setType("gps")
                .setType("new Title 2")
                .setHidden(true)
                .setNoSchedule(true)
                .setObservationGroupId(observationGroupId2)
        );

        assertThat(observationRepository.listObservations(studyId))
                .as("List all Observations")
                .hasSize(4);

        assertThat(observationRepository.listObservationsForGroup(studyId, studyGroupId))
                .as("Include group-specific observations and globals") //NOTE: and in no observation group
                .hasSize(2)
                .extracting(Observation::getObservationId)
                .containsOnly(observationResponse.getObservationId(), observationResponse2.getObservationId());

        assertThat(observationRepository.listObservationsForGroup(studyId, -1))
                .as("Non-existing Group should only retrieve 'global' observations")
                .hasSize(1)
                .extracting(Observation::getObservationId)
                .containsExactly(observationResponse2.getObservationId());

        assertThat(observationRepository.listObservationsForGroup(studyId, null))
                .as("<null>-Group should only retrieve 'global' observations")
                .hasSize(1)
                .as("Check for the global observation")
                .extracting(Observation::getObservationId)
                .containsExactly(observationResponse2.getObservationId());

        //list Observations for any study-group and no observation-group of observation-group 'observationGroupId1' ->
        //this is true for obs2 (no group at all) and obs3 (in the correct observation group)
        assertThat(observationRepository.listObservationsForGroup(studyId, null, Set.of(observationGroupId1)))
                .as("Obseration-Group should only retrieve observation in that observation group")
                .hasSize(2)
                .extracting(Observation::getObservationId)
                .containsExactly(observationResponse2.getObservationId(), observationResponse3a.getObservationId());


        //list Observations for study-group '-1' and no observation-group of observation-group 'observationGroupId1' ->
        // This is true for all expect 'obs1'
        assertThat(observationRepository.listObservationsForGroup(studyId, -1, Set.of(observationGroupId1, observationGroupId2)))
                .as("Obseration-Group should only retrieve observation in that observation group")
                .hasSize(3)
                .extracting(Observation::getObservationId)
                .containsExactly(observationResponse2.getObservationId(), observationResponse3a.getObservationId(), observationResponse3b.getObservationId());

        //test relative events
        observation.setSchedule(new RelativeEvent()
                .setDtstart(new RelativeDate().setOffset(new Duration().setValue(1).setUnit(Duration.Unit.DAY)).setTime(LocalTime.parse("12:00:00")))
                .setDtend(new RelativeDate().setOffset(new Duration().setValue(2).setUnit(Duration.Unit.DAY)).setTime(LocalTime.parse("13:00:00"))));
        Observation observationResponse4 = observationRepository.insert(observation);
        assertThat(observationResponse4.getSchedule())
                .isInstanceOf(RelativeEvent.class);

        //assert delete Observation Group sets property of Observation to null
        observationGroupRepository.deleteById(studyId, observationGroupId1);
        assertThat((observationRepository.getById(studyId, observationResponse3a.getObservationId()).getObservationGroupId()))
                .isNull();

        // Delete the group specific observations
        observationRepository.deleteObservation(studyId, observationResponse4.getObservationId());
        observationRepository.deleteObservation(studyId, observationResponse.getObservationId());
        assertThat((observationRepository.listObservations(studyId)))
                .hasSize(3);
        assertThat((observationRepository.listObservationsForGroup(studyId, studyGroupId)))
                .hasSize(2) //now that we deleted observationGroupId1 -> observationResponse3a is also in no group
                .extracting(Observation::getObservationId)
                .containsExactly(observationResponse2.getObservationId(), observationResponse3a.getObservationId());
        observationRepository.deleteObservation(studyId, observationResponse2.getObservationId());
        assertThat((observationRepository.listObservations(studyId)))
                .hasSize(2);

    }

    @Test
    @DisplayName("Participant Observations are inserted, returned, updated and deleted")
    public void testParticipantObservationProperties() {
        Long s1 = studyRepository.insert(new Study().setContact(new Contact().setPerson("test").setEmail("test"))).getStudyId();
        Integer o1 = observationRepository.insert(new Observation().setStudyId(s1).setType("t1").setHidden(true)).getObservationId();
        Integer o2 = observationRepository.insert(new Observation().setStudyId(s1).setType("t2").setHidden(true)).getObservationId();
        Integer p1 = participantRepository.insert(new Participant().setStudyId(s1).setRegistrationToken("t")).getParticipantId();

        ObservationProperties op1 = new ObservationProperties(Map.of("hello", "world"));
        ObservationProperties op2 = new ObservationProperties(Map.of("hello", "world2"));

        assertThat(observationRepository.getParticipantProperties(s1,p1,o1))
                .isEmpty();
        observationRepository.setParticipantProperties(s1, p1, o1, op1);
        assertThat(observationRepository.getParticipantProperties(s1,p1,o1))
                .get()
                .extracting(op -> op.getString("hello"))
                .isEqualTo("world");
        observationRepository.setParticipantProperties(s1, p1, o1, op2);
        assertThat(observationRepository.getParticipantProperties(s1,p1,o1))
                .get()
                .extracting(op -> op.getString("hello"))
                .isEqualTo("world2");
        observationRepository.setParticipantProperties(s1, p1, o2, op1);
        assertThat(observationRepository.getParticipantProperties(s1,p1,o1))
                .get()
                .extracting(op -> op.getString("hello"))
                .isEqualTo("world2");
        assertThat(observationRepository.getParticipantProperties(s1,p1,o2))
                .get()
                .extracting(op -> op.getString("hello"))
                .isEqualTo("world");
        observationRepository.removeParticipantProperties(s1, p1, o2);
        assertThat(observationRepository.getParticipantProperties(s1,p1,o2))
                .isEmpty();
        assertThat(observationRepository.getParticipantProperties(s1,p1,o1))
                .isPresent();
    }
}
