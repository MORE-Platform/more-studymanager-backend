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
        ObservationRepository.class, InterventionRepository.class, ParticipantRepository.class,
        JPAConfiguration.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test-containers-flyway")
class ObservationGroupRepositoryTest {

    @Autowired
    private ObservationGroupRepository observationGroupRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private ObservationRepository observationRepository;

    @Autowired
    private InterventionRepository interventionRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @BeforeEach
    void deleteAll() {
        observationGroupRepository.clear();
        observationRepository.clear();
        interventionRepository.clear();
        participantRepository.clear();
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

    @Test
    @DisplayName("Adding, removing and counting members in observation groups")
    public void testGroupMemberships() {
        Long studyId = studyRepository.insert(new Study().setContact(new Contact().setPerson("test").setEmail("test"))).getStudyId();

        ObservationGroup og = observationGroupRepository.insert(new ObservationGroup()
                .setStudyId(studyId)
                .setTitle("Test Group")
                .setPurpose("Test Purpose"));
        int ogId = og.getObservationGroupId();

        // Create observation
        Observation obs = new Observation()
                .setStudyId(studyId)
                .setTitle("Test Observation")
                .setPurpose("Test Purpose")
                .setParticipantInfo("Info")
                .setType("questionnaire")
                .setProperties(new ObservationProperties())
                .setSchedule(null); // Assuming null is allowed
        Observation obsResp = observationRepository.insert(obs);
        int obsId = obsResp.getObservationId();

        // Create intervention
        Intervention intv = new Intervention()
                .setStudyId(studyId)
                .setTitle("Test Intervention")
                .setPurpose("Test Purpose")
                .setSchedule(null); // Assuming null is allowed
        Intervention intvResp = interventionRepository.insert(intv);
        int intvId = intvResp.getInterventionId();

        // Create participant
        Participant p = new Participant()
                .setStudyId(studyId)
                .setAlias("Test Participant");
        Participant pResp = participantRepository.insert(p);
        int pId = pResp.getParticipantId();

        // Initial counts should be 0
        assertThat(observationGroupRepository.countObservationsInGroup(studyId, ogId)).isEqualTo(0);
        assertThat(observationGroupRepository.countInterventionsInGroup(studyId, ogId)).isEqualTo(0);
        assertThat(observationGroupRepository.countParticipantsInGroup(studyId, ogId)).isEqualTo(0);

        // Add to group
        observationGroupRepository.addObservationToGroup(studyId, obsId, ogId);
        observationGroupRepository.addInterventionToGroup(studyId, intvId, ogId);
        observationGroupRepository.addParticipantToGroup(studyId, pId, ogId);

        // Counts should be 1
        assertThat(observationGroupRepository.countObservationsInGroup(studyId, ogId)).isEqualTo(1);
        assertThat(observationGroupRepository.countInterventionsInGroup(studyId, ogId)).isEqualTo(1);
        assertThat(observationGroupRepository.countParticipantsInGroup(studyId, ogId)).isEqualTo(1);

        // Add again (should not increase due to ON CONFLICT DO NOTHING)
        observationGroupRepository.addObservationToGroup(studyId, obsId, ogId);
        observationGroupRepository.addInterventionToGroup(studyId, intvId, ogId);
        observationGroupRepository.addParticipantToGroup(studyId, pId, ogId);

        // Counts still 1
        assertThat(observationGroupRepository.countObservationsInGroup(studyId, ogId)).isEqualTo(1);
        assertThat(observationGroupRepository.countInterventionsInGroup(studyId, ogId)).isEqualTo(1);
        assertThat(observationGroupRepository.countParticipantsInGroup(studyId, ogId)).isEqualTo(1);

        // Remove from group
        observationGroupRepository.removeObservationFromGroup(studyId, obsId, ogId);
        observationGroupRepository.removeInterventionFromGroup(studyId, intvId, ogId);
        observationGroupRepository.removeParticipantFromGroup(studyId, pId, ogId);

        // Counts should be 0
        assertThat(observationGroupRepository.countObservationsInGroup(studyId, ogId)).isEqualTo(0);
        assertThat(observationGroupRepository.countInterventionsInGroup(studyId, ogId)).isEqualTo(0);
        assertThat(observationGroupRepository.countParticipantsInGroup(studyId, ogId)).isEqualTo(0);
    }

}
