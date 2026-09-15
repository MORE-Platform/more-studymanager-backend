/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.configuration.JPAConfiguration;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.exception.DataConstraintException;
import io.redlink.more.studymanager.model.Contact;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.ObservationResyncRequest;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.Study;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@EnableAutoConfiguration
@ContextConfiguration(classes = {
        ObservationResyncRequestRepository.class,
        StudyRepository.class, ParticipantRepository.class, ObservationRepository.class,
        JPAConfiguration.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles({"test", "test-containers-flyway"})
class ObservationResyncRequestRepositoryTest {

    @Autowired
    private ObservationResyncRequestRepository repository;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private ObservationRepository observationRepository;

    @BeforeEach
    void deleteAll() {
        repository.clear();
        observationRepository.clear();
        participantRepository.clear();
    }

    @Test
    @DisplayName("A resync request can be inserted, found, listed and deleted")
    void testInsertFindListDelete() {
        Long studyId = newStudy();
        int participantId = newParticipant(studyId, "P1");
        int observationId = newObservation(studyId, "Survey 1");
        int otherObservationId = newObservation(studyId, "Survey 2");

        assertThat(repository.find(studyId, participantId, observationId)).isEmpty();

        ObservationResyncRequest inserted = repository.insert(studyId, participantId, observationId);
        assertThat(inserted.studyId()).isEqualTo(studyId);
        assertThat(inserted.participantId()).isEqualTo(participantId);
        assertThat(inserted.observationId()).isEqualTo(observationId);
        assertThat(inserted.created()).isNotNull();

        assertThat(repository.find(studyId, participantId, observationId)).contains(inserted);

        repository.insert(studyId, participantId, otherObservationId);
        assertThat(repository.listByParticipant(studyId, participantId))
                .extracting(ObservationResyncRequest::observationId)
                .containsExactly(observationId, otherObservationId);

        repository.deleteByIds(studyId, participantId, observationId);
        assertThat(repository.find(studyId, participantId, observationId)).isEmpty();
        assertThat(repository.listByParticipant(studyId, participantId)).hasSize(1);
    }

    @Test
    @DisplayName("Inserting a second request for the same participant and observation is reported as a conflict")
    void testDuplicateInsert() {
        Long studyId = newStudy();
        int participantId = newParticipant(studyId, "P1");
        int observationId = newObservation(studyId, "Survey 1");

        repository.insert(studyId, participantId, observationId);

        assertThatThrownBy(() -> repository.insert(studyId, participantId, observationId))
                .isInstanceOf(DataConstraintException.class);
    }

    @Test
    @DisplayName("Inserting for an unknown participant or observation is rejected as a bad request")
    void testInsertWithUnknownReferences() {
        Long studyId = newStudy();
        int participantId = newParticipant(studyId, "P1");
        int observationId = newObservation(studyId, "Survey 1");

        assertThatThrownBy(() -> repository.insert(studyId, participantId + 99, observationId))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> repository.insert(studyId, participantId, observationId + 99))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Deleting the participant cascades to its resync requests")
    void testCascadeOnParticipantDelete() {
        Long studyId = newStudy();
        int participantId = newParticipant(studyId, "P1");
        int observationId = newObservation(studyId, "Survey 1");

        repository.insert(studyId, participantId, observationId);
        assertThat(repository.find(studyId, participantId, observationId)).isPresent();

        participantRepository.deleteParticipant(studyId, participantId);

        assertThat(repository.find(studyId, participantId, observationId)).isEmpty();
    }

    private Long newStudy() {
        return studyRepository.insert(new Study().setContact(new Contact().setPerson("test").setEmail("test"))).getStudyId();
    }

    private int newParticipant(Long studyId, String alias) {
        return participantRepository.insert(new Participant()
                .setStudyId(studyId).setAlias(alias).setRegistrationToken("rt_" + alias)).getParticipantId();
    }

    private int newObservation(Long studyId, String title) {
        return observationRepository.insert(new Observation()
                .setStudyId(studyId)
                .setTitle(title)
                .setPurpose("Test Purpose")
                .setParticipantInfo("Info")
                .setType("lime-survey-observation")
                .setHidden(false)
                .setProperties(new ObservationProperties())
                .setSchedule(null)).getObservationId();
    }
}
