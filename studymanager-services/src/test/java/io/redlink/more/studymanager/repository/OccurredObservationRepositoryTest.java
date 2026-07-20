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
import io.redlink.more.studymanager.core.datavalidity.ObservationDataState;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.properties.OccurredObservationProperties;
import io.redlink.more.studymanager.model.*;
import io.redlink.more.studymanager.model.scheduler.Event;
import io.redlink.more.studymanager.model.scheduler.RecurrenceRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@EnableAutoConfiguration
@ContextConfiguration(classes = {
        OccurredObservationRepository.class, StudyRepository.class, ParticipantRepository.class,
        StudyGroupRepository.class, ObservationRepository.class, JPAConfiguration.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test-containers-flyway")
class OccurredObservationRepositoryTest {

    @Autowired
    private OccurredObservationRepository occurredObservationRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private ObservationRepository observationRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @BeforeEach
    void deleteAll() {
        occurredObservationRepository.clear();
        observationRepository.clear();
        participantRepository.clear();
        studyGroupRepository.clear();
        studyRepository.clear();
    }

    @Test
    @DisplayName("ParticipantObservations are inserted, updated, listed and deleted from database")
    public void testInsertListUpdateDelete() {
        String type = "accelerometer";
        Long studyId = studyRepository.insert(new Study().setContact(new Contact().setPerson("test").setEmail("test"))).getStudyId();
        Integer studyGroupId = studyGroupRepository.insert(new StudyGroup().setStudyId(studyId)).getStudyGroupId();
        Instant startTime = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        Instant endTime = startTime.plus(2, ChronoUnit.HOURS);

        Observation observation = observationRepository.insert(new Observation()
                .setStudyId(studyId)
                .setType(type)
                .setTitle("Accelerometer Observation")
                .setStudyGroupId(studyGroupId)
                .setProperties(new ObservationProperties(Map.of("testProperty", "testValue")))
                .setSchedule(new Event()
                        .setDateStart(startTime)
                        .setDateEnd(endTime)
                        .setRRule(new RecurrenceRule().setFreq("DAILY").setCount(7)))
                .setHidden(false)
                .setNoSchedule(false));

        Participant participant = participantRepository.insert(new Participant()
                .setAlias("participant x")
                .setStudyGroupId(studyGroupId)
                .setStudyId(studyId)
                .setRegistrationToken("TEST123"));

        var occurredObservation = new OccurredObservation(
                studyId,
                observation.getObservationId(),
                participant.getParticipantId(),
                startTime,
                endTime);

        var occurredObservationResponse = occurredObservationRepository.upsert(occurredObservation);

        assertThat(occurredObservationResponse).isNotNull();
        assertThat(occurredObservationResponse.studyId()).isEqualTo(studyId);
        assertThat(occurredObservationResponse.observationId()).isEqualTo(observation.getObservationId());
        assertThat(occurredObservationResponse.participantId()).isEqualTo(participant.getParticipantId());
        assertThat(occurredObservationResponse.start()).isEqualTo(startTime);
        assertThat(occurredObservationResponse.end()).isEqualTo(endTime);
        assertThat(occurredObservationResponse.dataValid()).isTrue();
        assertThat(occurredObservationResponse.dataState()).isEqualTo(ObservationDataState.MISSING);
        assertThat(occurredObservationResponse.properties()).isNotNull();
        assertThat(occurredObservationResponse.properties()).isEmpty();
        assertThat(occurredObservationResponse.created()).isNotNull();
        assertThat(occurredObservationResponse.modified()).isNotNull();
        assertThat(occurredObservationResponse.created()).isAfter(startTime);
        assertThat(occurredObservationResponse.modified()).isEqualTo(occurredObservationResponse.created());

        var ooGetById = occurredObservationRepository.getByIds(studyId, participant.getParticipantId(), observation.getObservationId(), startTime);
        assertThat(ooGetById).isNotNull();
        assertThat(ooGetById.studyId()).isEqualTo(studyId);
        assertThat(ooGetById.participantId()).isEqualTo(participant.getParticipantId());
        assertThat(ooGetById.observationId()).isEqualTo(observation.getObservationId());
        assertThat(ooGetById.start()).isEqualTo(startTime);

        var getByIdNotFound1 = occurredObservationRepository.getByIds(999, participant.getParticipantId(), observation.getObservationId(), startTime);
        assertThat(getByIdNotFound1).isNull();
        var getByIdNotFound2 = occurredObservationRepository.getByIds(studyId, 999, observation.getObservationId(), startTime);
        assertThat(getByIdNotFound2).isNull();
        var getByIdNotFound3 = occurredObservationRepository.getByIds(studyId, participant.getParticipantId(), 999, startTime);
        assertThat(getByIdNotFound3).isNull();
        var getByIdNotFound4 = occurredObservationRepository.getByIds(studyId, participant.getParticipantId(), observation.getObservationId(), startTime.plusSeconds(1));
        assertThat(getByIdNotFound4).isNull();

        var updatedOccurentObservation = new OccurredObservation(
                occurredObservationResponse.studyId(),
                occurredObservationResponse.observationId(),
                occurredObservationResponse.participantId(),
                occurredObservationResponse.start(),
                occurredObservationResponse.end(),
                false, //change data valid
                ObservationDataState.INCOMPLETE,
                new OccurredObservationProperties(Map.of("test", "dummy", "testInt", 69))
        );

        var updatedOccurentObservationResponse = occurredObservationRepository.update(updatedOccurentObservation);

        assertThat(updatedOccurentObservationResponse).isNotNull();
        assertThat(updatedOccurentObservationResponse.studyId()).isEqualTo(studyId);
        assertThat(updatedOccurentObservationResponse.observationId()).isEqualTo(observation.getObservationId());
        assertThat(updatedOccurentObservationResponse.participantId()).isEqualTo(participant.getParticipantId());
        assertThat(updatedOccurentObservationResponse.start()).isEqualTo(startTime);
        assertThat(updatedOccurentObservationResponse.end()).isEqualTo(endTime);
        assertThat(updatedOccurentObservationResponse.dataValid()).isFalse(); //UPDATED
        assertThat(updatedOccurentObservationResponse.dataState()).isEqualTo(ObservationDataState.INCOMPLETE);
        assertThat(updatedOccurentObservationResponse.properties()).isNotNull();
        assertThat(updatedOccurentObservationResponse.properties()).hasSize(2);
        assertThat(updatedOccurentObservationResponse.properties().getString("test")).isEqualTo("dummy");
        assertThat(updatedOccurentObservationResponse.properties().getInt("testInt")).isEqualTo(69);
        assertThat(updatedOccurentObservationResponse.created()).isNotNull();
        assertThat(updatedOccurentObservationResponse.modified()).isNotNull();
        assertThat(updatedOccurentObservationResponse.created()).isEqualTo(occurredObservationResponse.created());
        assertThat(updatedOccurentObservationResponse.modified()).isAfter(updatedOccurentObservationResponse.created());

        //assert insertOrGet and validate that only the end is updated
        var duplicateInsertResponse = occurredObservationRepository.upsert(new OccurredObservation(
                updatedOccurentObservationResponse.studyId(),
                updatedOccurentObservationResponse.observationId(),
                updatedOccurentObservationResponse.participantId(),
                updatedOccurentObservationResponse.start(),
                updatedOccurentObservationResponse.end().plus(1, ChronoUnit.HOURS),
                true, //change data valid
                ObservationDataState.COMPLETE,
                new OccurredObservationProperties(Map.of("test", "dummy-changed", "testInt", 42))
        ));

        assertThat(duplicateInsertResponse).isNotNull();
        assertThat(duplicateInsertResponse.studyId()).isEqualTo(studyId);
        assertThat(duplicateInsertResponse.observationId()).isEqualTo(observation.getObservationId());
        assertThat(duplicateInsertResponse.participantId()).isEqualTo(participant.getParticipantId());
        assertThat(duplicateInsertResponse.start()).isEqualTo(startTime);
        assertThat(duplicateInsertResponse.end()).isEqualTo(endTime.plus(1, ChronoUnit.HOURS)); //UPDATED
        //NOT UPDATED FIELDS!!
        assertThat(duplicateInsertResponse.dataValid()).isFalse(); //NOT TRUE
        assertThat(duplicateInsertResponse.dataState()).isEqualTo(ObservationDataState.INCOMPLETE); //NOT COMPLETE
        assertThat(duplicateInsertResponse.properties()).isNotNull();
        assertThat(duplicateInsertResponse.properties()).hasSize(2);
        assertThat(duplicateInsertResponse.properties().getString("test")).isEqualTo("dummy"); //NOT dummy-changed
        assertThat(duplicateInsertResponse.properties().getInt("testInt")).isEqualTo(69); // NOT 42
        assertThat(duplicateInsertResponse.created()).isNotNull();
        assertThat(duplicateInsertResponse.modified()).isNotNull();
        assertThat(duplicateInsertResponse.created()).isEqualTo(occurredObservationResponse.created());
        assertThat(duplicateInsertResponse.modified()).isAfter(updatedOccurentObservationResponse.created());

        //for list we need some more variations
        var startTime2 = startTime.minus(3, ChronoUnit.HOURS);
        var endTime2 = endTime.minus(3, ChronoUnit.HOURS);

        Observation observation2 = observationRepository.insert(new Observation()
                .setStudyId(studyId)
                .setType(type)
                .setTitle("Accelerometer Observation 2")
                .setStudyGroupId(studyGroupId)
                .setProperties(new ObservationProperties(Map.of("testProperty", "testValue")))
                .setSchedule(new Event()
                        .setDateStart(startTime2)
                        .setDateEnd(endTime2)
                        .setRRule(new RecurrenceRule().setFreq("DAILY").setCount(10)))
                .setHidden(false)
                .setNoSchedule(false));

        Participant participant2 = participantRepository.insert(new Participant()
                .setAlias("participant y")
                .setStudyGroupId(studyGroupId)
                .setStudyId(studyId)
                .setRegistrationToken("TEST234"));
        //Oservation 1, Participant 1, Today
        var ooO1P1T = duplicateInsertResponse; //the existing one
        //Oservation 1, Participant 1, Yesterday
        var ooO1P1Y = occurredObservationRepository.upsert(new OccurredObservation(
                studyId, observation.getObservationId(), participant.getParticipantId(), startTime.minus(1, ChronoUnit.DAYS), endTime.minus(1, ChronoUnit.DAYS),
                true, ObservationDataState.COMPLETE, new OccurredObservationProperties()));
        var ooO1P2T = occurredObservationRepository.upsert(new OccurredObservation(
                studyId, observation.getObservationId(), participant2.getParticipantId(), startTime, endTime,
                true, ObservationDataState.MISSING, new OccurredObservationProperties()));
        var ooO1P2Y = occurredObservationRepository.upsert(new OccurredObservation(
                studyId, observation.getObservationId(), participant2.getParticipantId(), startTime.minus(1, ChronoUnit.DAYS), endTime.minus(1, ChronoUnit.DAYS),
                true, ObservationDataState.MISSING, new OccurredObservationProperties()));
        var ooO2P1T = occurredObservationRepository.upsert(new OccurredObservation(
                studyId, observation2.getObservationId(), participant.getParticipantId(), startTime2, endTime2,
                true, ObservationDataState.INCOMPLETE, new OccurredObservationProperties()));
        var ooO2P1Y = occurredObservationRepository.upsert(new OccurredObservation(
                studyId, observation2.getObservationId(), participant.getParticipantId(), startTime2.minus(1, ChronoUnit.DAYS), endTime2.minus(1, ChronoUnit.DAYS),
                true, ObservationDataState.COMPLETE, new OccurredObservationProperties()));
        var ooO2P2T = occurredObservationRepository.upsert(new OccurredObservation(
                studyId, observation2.getObservationId(), participant2.getParticipantId(), startTime2, endTime2,
                true, ObservationDataState.PARTIAL, new OccurredObservationProperties()));
        var ooO2P2Y = occurredObservationRepository.upsert(new OccurredObservation(
                studyId, observation2.getObservationId(), participant2.getParticipantId(), startTime2.minus(1, ChronoUnit.DAYS), endTime2.minus(1, ChronoUnit.DAYS),
                false, ObservationDataState.COMPLETE, new OccurredObservationProperties()));

        //List OccurredObservations for Participant 1
        var p1oos = occurredObservationRepository.listOccurredObservations(studyId, participant.getParticipantId(), null, null, null).toList();
        assertThat(p1oos).isNotNull();
        assertThat(p1oos).hasSize(4);
        assertThat(p1oos).containsExactlyInAnyOrder(ooO1P1T, ooO1P1Y, ooO2P1T, ooO2P1Y);
        //List OccurredObservations for Participant 2 and Observation 1
        var o1p2oos = occurredObservationRepository.listOccurredObservations(studyId, participant2.getParticipantId(), observation.getObservationId(), null, null).toList();
        assertThat(o1p2oos).isNotNull();
        assertThat(o1p2oos).hasSize(2);
        assertThat(o1p2oos).containsExactlyInAnyOrder(ooO1P2T, ooO1P2Y);
        //List OccurredObservations in the State Incomplete or partial
        var sIPoos = occurredObservationRepository.listOccurredObservations(studyId, null, null, null, EnumSet.of(ObservationDataState.INCOMPLETE, ObservationDataState.PARTIAL)).toList();
        assertThat(sIPoos).isNotNull();
        assertThat(sIPoos).hasSize(3);
        assertThat(sIPoos).containsExactlyInAnyOrder(ooO1P1T, ooO2P1T, ooO2P2T);
        //List OccurredObservations with invalid Data
        var vIPoos = occurredObservationRepository.listOccurredObservations(studyId, null, null, false, null).toList();
        assertThat(vIPoos).isNotNull();
        assertThat(vIPoos).hasSize(2);
        assertThat(vIPoos).containsExactlyInAnyOrder(ooO1P1T, ooO2P2Y);

        // Test startTime filter only (start >= startTime) → should exclude yesterday's records
        var startTimeFiltered = occurredObservationRepository.listOccurredObservations(
                studyId, participant.getParticipantId(), null, null, null,
                startTime, null).toList();

        assertThat(startTimeFiltered)
                .as("startTime filter should return only observations starting on/after startTime (today)")
                .hasSize(1)
                .containsExactlyInAnyOrder(ooO1P1T);

        // Test endTime filter only (end <= endTime2) → should only include the earlier observation2 records
        var endTimeFiltered = occurredObservationRepository.listOccurredObservations(
                studyId, participant.getParticipantId(), null, null, null,
                null, endTime.minus(1, ChronoUnit.DAYS)).toList();

        assertThat(endTimeFiltered)
                .as("endTime filter should return only observations ending on/before (endTime - 1DAY)")
                .hasSize(2)
                .containsExactlyInAnyOrder(ooO1P1Y, ooO2P1Y);

        // Test both startTime and endTime together (narrow window around today's observation 1)
        var timeRangeFiltered = occurredObservationRepository.listOccurredObservations(
                studyId, participant.getParticipantId(), null, null, null,
                startTime.minus(1, ChronoUnit.DAYS), endTime).toList();

        assertThat(timeRangeFiltered)
                .as("combined (startTime - 1Day) + endTime should return two observations")
                .hasSize(2)
                .containsExactlyInAnyOrder(ooO1P1Y, ooO2P1T);

        // Test time range that should return no results
        var futureRange = occurredObservationRepository.listOccurredObservations(
                studyId, null, null, null, null,
                startTime.plus(5, ChronoUnit.DAYS), null).toList();
        assertThat(futureRange)
                .as("future startTime should return empty result")
                .isEmpty();

        // Test time range combined with other filters (participant + dataValid)
        var combinedFilter = occurredObservationRepository.listOccurredObservations(
                studyId, participant2.getParticipantId(), null, true, null,
                startTime, endTime.plus(1, ChronoUnit.HOURS)).toList();

        assertThat(combinedFilter)
                .as("time range + participant + dataValid filter")
                .hasSize(1)
                .containsExactly(ooO1P2T);

        //Test Query for last Start Time
        var lastStartTimeStudy = occurredObservationRepository.getLatestStartTime(studyId, null, null, null, null);
        assertThat(lastStartTimeStudy).isNotNull();
        assertThat(lastStartTimeStudy).isEqualTo(startTime);
        var lastStartTimeStateP2StateComplete =  occurredObservationRepository.getLatestStartTime(studyId, participant2.getParticipantId(), null, null, EnumSet.of(ObservationDataState.COMPLETE));
        assertThat(lastStartTimeStateP2StateComplete).isNotNull();
        assertThat(lastStartTimeStateP2StateComplete).isEqualTo(startTime2.minus(1, ChronoUnit.DAYS));
        //validate that searching for a combination with no entry returns null
        var lastStartTimeNotFound = occurredObservationRepository.getLatestStartTime(studyId, -1, null, null, null);
        assertThat(lastStartTimeNotFound).isNull();

        //Test deletion by studyId
        occurredObservationRepository.deleteOccurredObservations(studyId);
        assertThat(occurredObservationRepository.listOccurredObservations(studyId, null, null, null, null).toList()).isEmpty();
    }

}
