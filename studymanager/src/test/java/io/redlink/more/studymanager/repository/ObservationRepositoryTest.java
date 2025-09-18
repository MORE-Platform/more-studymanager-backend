/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.model.*;
import io.redlink.more.studymanager.model.scheduler.*;
import io.redlink.more.studymanager.utils.MapperUtils;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    private ParticipantRepository participantRepository;

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
        Long studyId = studyRepository.insert(new Study().setContact(new Contact().setPerson("test").setEmail("test"))).getStudyId();
        Integer studyGroupId = studyGroupRepository.insert(new StudyGroup().setStudyId(studyId)).getStudyGroupId();
        Instant startTime = Instant.now();
        Instant endTime = Instant.now().plus(2, ChronoUnit.HOURS);

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
                .setNoSchedule(false);

        Observation observationResponse = observationRepository.insert(observation);

        assertThat(observationResponse.getObservationId()).isNotNull();
        assertThat(observationResponse.getTitle()).isEqualTo(observation.getTitle());
        assertThat(observationResponse.getProperties()).isEqualTo(observation.getProperties());
        assertThat(MapperUtils.writeValueAsString(observationResponse.getSchedule()))
                .isEqualTo(MapperUtils.writeValueAsString(observation.getSchedule()));

        Integer oldId = observationResponse.getObservationId();

        observationResponse.setType("new type")
                .setTitle("some new title")
                .setSchedule(new Event().setDateEnd(Instant.now()).setDateEnd(Instant.now().plusSeconds(60)));

        Observation compareObservationResponse = observationRepository.updateObservation(observationResponse);

        assertThat(compareObservationResponse.getTitle()).isEqualTo(observationResponse.getTitle());
        assertThat(compareObservationResponse.getType()).isEqualTo(type);
        assertThat(compareObservationResponse.getObservationId()).isEqualTo(oldId);
        assertThat(compareObservationResponse.getSchedule()).isNotEqualTo(observation.getSchedule());

        Observation observationResponse2 = observationRepository.insert(new Observation()
                .setStudyId(studyId)
                .setType("gps")
                .setType("new Title")
                .setHidden(true)
                .setNoSchedule(true)
        );

        assertThat((observationRepository.listObservations(studyId)))
                .as("List all Observations")
                .hasSize(2);
        assertThat((observationRepository.listObservationsForGroup(studyId, studyGroupId)))
                .as("Include group-specific observations and globals")
                .hasSize(2);
        assertThat((observationRepository.listObservationsForGroup(studyId, -1)))
                .as("Non-existing Group should only retrieve 'global' observations")
                .hasSize(1);
        assertThat((observationRepository.listObservationsForGroup(studyId, null)))
                .as("<null>-Group should only retrieve 'global' observations")
                .hasSize(1)
                .as("Check for the global observation")
                .extracting(Observation::getObservationId)
                .contains(observationResponse2.getObservationId());
        // Delete the group specific observations
        observationRepository.deleteObservation(studyId, observationResponse.getObservationId());
        assertThat((observationRepository.listObservations(studyId)))
                .hasSize(1);
        assertThat((observationRepository.listObservationsForGroup(studyId, studyGroupId)))
                .hasSize(1);
        observationRepository.deleteObservation(studyId, observationResponse2.getObservationId());
        assertThat((observationRepository.listObservations(studyId)))
                .hasSize(0);

        observation.setSchedule(new RelativeEvent()
                .setDtstart(new RelativeDate().setOffset(new Duration().setValue(1).setUnit(Duration.Unit.DAY)).setTime(LocalTime.parse("12:00:00")))
                .setDtend(new RelativeDate().setOffset(new Duration().setValue(2).setUnit(Duration.Unit.DAY)).setTime(LocalTime.parse("13:00:00"))));

        Observation observationResponse3 = observationRepository.insert(observation);
        assertThat(observationResponse3.getSchedule())
                .isInstanceOf(RelativeEvent.class);
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
