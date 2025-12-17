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
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@EnableAutoConfiguration
@ContextConfiguration(classes = {
        ObservationGroupRepository.class, StudyRepository.class,
        JPAConfiguration.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test-containers-flyway")
class ObservationGroupRepositoryTest {

    @Autowired
    private ObservationGroupRepository observationGroupRepository;

    @Autowired
    private StudyRepository studyRepository;

    @BeforeEach
    void deleteAll() {
        observationGroupRepository.clear();
    }

    @Test
    @DisplayName("ObservationGroups are inserted, updated, listed and deleted from database")
    public void testInsertListUpdateDelete() throws InterruptedException {

        Long studyId = studyRepository.insert(new Study().setContact(new Contact().setPerson("test").setEmail("test"))).getStudyId();

        //INSERT
        ObservationGroup observationGroup = new ObservationGroup()
                .setStudyId(studyId)
                .setTitle("Test Observation Group")
                .setPurpose("Test Purpose");

        ObservationGroup observationGroupResponse = observationGroupRepository.insert(observationGroup);

        assertThat(observationGroupResponse.getObservationGroupId()).isNotNull();
        assertThat(observationGroupResponse.getStudyId()).isEqualTo(observationGroup.getStudyId());
        assertThat(observationGroupResponse.getTitle()).isEqualTo(observationGroup.getTitle());
        assertThat(observationGroupResponse.getPurpose()).isEqualTo(observationGroup.getPurpose());
        assertThat(observationGroupResponse.getCreated()).isNotNull();
        assertThat(observationGroupResponse.getModified()).isNotNull();
        assertThat(observationGroupResponse.getCreated()).isEqualTo(observationGroupResponse.getModified());

        //UPDATE
        observationGroupResponse
                .setTitle("Updated Observation Group")
                .setPurpose("Updated Purpose");

        TimeUnit.MICROSECONDS.sleep(1); //to ensure a different modified time

        ObservationGroup compareObservationGroupResponse = observationGroupRepository.update(observationGroupResponse);

        assertThat(compareObservationGroupResponse.getTitle()).isEqualTo(observationGroupResponse.getTitle());
        assertThat(compareObservationGroupResponse.getPurpose()).isEqualTo(observationGroupResponse.getPurpose());
        assertThat(compareObservationGroupResponse.getStudyId()).isEqualTo(observationGroupResponse.getStudyId());
        assertThat(compareObservationGroupResponse.getObservationGroupId()).isEqualTo(observationGroupResponse.getObservationGroupId());
        assertThat(compareObservationGroupResponse.getCreated()).isEqualTo(observationGroupResponse.getCreated());
        assertThat(compareObservationGroupResponse.getModified()).isAfter(observationGroupResponse.getModified());

        ObservationGroup observationGroup2Response = observationGroupRepository.insert(new ObservationGroup()
                .setStudyId(studyId)
                .setTitle("Test Observation Group 2")
                .setPurpose("Test Purpose 2"));

        Long studyId2 = studyRepository.insert(new Study().setContact(new Contact().setPerson("test3").setEmail("test3"))).getStudyId();

        ObservationGroup observationGroupStudy2Response = observationGroupRepository.insert(new ObservationGroup()
                .setStudyId(studyId2)
                .setTitle("Test Observation Group Study 2")
                .setPurpose("Test Purpose 3"));

        List<ObservationGroup> study1ObserationGroups = observationGroupRepository.listObservationGroupsOrderedByObservationGroupIdAsc(studyId);
        assertThat(study1ObserationGroups.size()).isEqualTo(2);
        assertThat(study1ObserationGroups.get(0).getStudyId()).isEqualTo(studyId);
        assertThat(study1ObserationGroups.get(0).getObservationGroupId()).isEqualTo(observationGroupResponse.getObservationGroupId());
        assertThat(study1ObserationGroups.get(1).getStudyId()).isEqualTo(studyId);
        assertThat(study1ObserationGroups.get(1).getObservationGroupId()).isEqualTo(observationGroup2Response.getObservationGroupId());

        List<ObservationGroup> study2ObserationGroups = observationGroupRepository.listObservationGroupsOrderedByObservationGroupIdAsc(studyId2);
        assertThat(study2ObserationGroups.size()).isEqualTo(1);
        assertThat(study2ObserationGroups.get(0).getStudyId()).isEqualTo(studyId2);
        assertThat(study2ObserationGroups.get(0).getObservationGroupId()).isEqualTo(observationGroupStudy2Response.getObservationGroupId());

        // Delete the group specific observations
        observationGroupRepository.deleteById(studyId, observationGroup2Response.getObservationGroupId());
        assertThat(observationGroupRepository.listObservationGroupsOrderedByObservationGroupIdAsc(studyId))
                .hasSize(1);
    }
}
