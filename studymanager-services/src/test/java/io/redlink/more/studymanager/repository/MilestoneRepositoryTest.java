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
import io.redlink.more.studymanager.model.Contact;
import io.redlink.more.studymanager.model.Milestone;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.ParticipantMilestone;
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

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@EnableAutoConfiguration
@ContextConfiguration(classes = {
        MilestoneRepository.class, ParticipantMilestoneRepository.class,
        StudyRepository.class, ParticipantRepository.class,
        JPAConfiguration.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles({"test", "test-containers-flyway"})
class MilestoneRepositoryTest {

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private ParticipantMilestoneRepository participantMilestoneRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @BeforeEach
    void deleteAll() {
        participantMilestoneRepository.clear();
        milestoneRepository.clear();
        participantRepository.clear();
    }

    @Test
    @DisplayName("Milestones get an incrementing milestoneId and orderIndex per study, and can be listed, updated and deleted")
    void testInsertListUpdateDelete() {
        Long studyId = studyRepository.insert(new Study().setContact(new Contact().setPerson("test").setEmail("test"))).getStudyId();

        Milestone m1 = milestoneRepository.insert(new Milestone().setStudyId(studyId).setName("Baseline"));
        Milestone m2 = milestoneRepository.insert(new Milestone().setStudyId(studyId).setName("Follow-up"));
        Milestone m3 = milestoneRepository.insert(new Milestone().setStudyId(studyId).setName("End"));

        assertThat(m1.getMilestoneId()).isEqualTo(1);
        assertThat(m1.getOrderIndex()).isEqualTo(1);
        assertThat(m2.getMilestoneId()).isEqualTo(2);
        assertThat(m2.getOrderIndex()).isEqualTo(2);
        assertThat(m3.getMilestoneId()).isEqualTo(3);
        assertThat(m3.getOrderIndex()).isEqualTo(3);
        assertThat(m1.getCreated()).isNotNull();

        assertThat(milestoneRepository.listMilestonesOrderedByOrderIndexAsc(studyId))
                .extracting(Milestone::getMilestoneId)
                .containsExactly(1, 2, 3);

        Milestone updated = milestoneRepository.update(new Milestone().setStudyId(studyId).setMilestoneId(2).setName("Mid-study"));
        assertThat(updated.getName()).isEqualTo("Mid-study");
        assertThat(updated.getOrderIndex()).isEqualTo(2);

        // deleting the middle milestone and decrementing trailing order indices
        milestoneRepository.deleteById(studyId, 2);
        milestoneRepository.decrementOrderIndexAbove(studyId, 2);

        List<Milestone> remaining = milestoneRepository.listMilestonesOrderedByOrderIndexAsc(studyId);
        assertThat(remaining).extracting(Milestone::getMilestoneId).containsExactly(1, 3);
        assertThat(remaining).extracting(Milestone::getOrderIndex).containsExactly(1, 2);
    }

    @Test
    @DisplayName("Milestones can be reordered by shifting the range in between the old and new position")
    void testReorderMilestones() {
        Long studyId = studyRepository.insert(new Study().setContact(new Contact().setPerson("test").setEmail("test"))).getStudyId();
        Milestone m1 = milestoneRepository.insert(new Milestone().setStudyId(studyId).setName("M1"));
        Milestone m2 = milestoneRepository.insert(new Milestone().setStudyId(studyId).setName("M2"));
        Milestone m3 = milestoneRepository.insert(new Milestone().setStudyId(studyId).setName("M3"));

        assertThat(milestoneRepository.countByStudyId(studyId)).isEqualTo(3);

        // move m1 (orderIndex 1) to the last position (orderIndex 3): m2 and m3 shift down by one
        milestoneRepository.shiftOrderIndexRange(studyId, 2, 3, -1);
        milestoneRepository.setOrderIndex(studyId, m1.getMilestoneId(), 3);

        List<Milestone> reordered = milestoneRepository.listMilestonesOrderedByOrderIndexAsc(studyId);
        assertThat(reordered).extracting(Milestone::getMilestoneId).containsExactly(m2.getMilestoneId(), m3.getMilestoneId(), m1.getMilestoneId());
        assertThat(reordered).extracting(Milestone::getOrderIndex).containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("Active participant milestones are counted for the delete-guard")
    void testCountActiveParticipantMilestones() {
        Long studyId = studyRepository.insert(new Study().setContact(new Contact().setPerson("test").setEmail("test"))).getStudyId();
        Milestone milestone = milestoneRepository.insert(new Milestone().setStudyId(studyId).setName("Baseline"));

        Participant participant = participantRepository.insert(new Participant()
                .setStudyId(studyId).setAlias("P1").setRegistrationToken("rt_p1"));

        assertThat(milestoneRepository.countActiveParticipantMilestones(studyId, milestone.getMilestoneId())).isZero();

        participantMilestoneRepository.insert(new ParticipantMilestone()
                .setStudyId(studyId)
                .setParticipantId(participant.getParticipantId())
                .setMilestoneId(milestone.getMilestoneId())
                .setDateTime(Instant.parse("2024-06-15T09:00:00Z")));

        // not yet active -> must not block deletion
        assertThat(milestoneRepository.countActiveParticipantMilestones(studyId, milestone.getMilestoneId())).isZero();

        participantRepository.setStatusByIds(studyId, participant.getParticipantId(), Participant.Status.ACTIVE);

        assertThat(milestoneRepository.countActiveParticipantMilestones(studyId, milestone.getMilestoneId())).isEqualTo(1);
    }

    @Test
    @DisplayName("A participant milestone is inserted, updated and looked up by (study, participant, milestone)")
    void testParticipantMilestoneInsertUpdateExists() {
        Long studyId = studyRepository.insert(new Study().setContact(new Contact().setPerson("test").setEmail("test"))).getStudyId();
        Milestone milestone = milestoneRepository.insert(new Milestone().setStudyId(studyId).setName("Baseline"));
        Participant participant = participantRepository.insert(new Participant()
                .setStudyId(studyId).setAlias("P1").setRegistrationToken("rt_p1"));

        assertThat(participantMilestoneRepository.exists(studyId, participant.getParticipantId(), milestone.getMilestoneId())).isFalse();

        Instant dateTime = Instant.parse("2024-06-15T09:00:00Z");
        ParticipantMilestone inserted = participantMilestoneRepository.insert(new ParticipantMilestone()
                .setStudyId(studyId)
                .setParticipantId(participant.getParticipantId())
                .setMilestoneId(milestone.getMilestoneId())
                .setDateTime(dateTime));

        assertThat(inserted.getDateTime()).isEqualTo(dateTime);
        assertThat(inserted.getCreated()).isNotNull();
        assertThat(inserted.getName()).as("the referenced Milestone's name is joined in").isEqualTo("Baseline");
        assertThat(inserted.getParticipantMilestoneId())
                .as("participantMilestoneId is its own generated id, distinct from milestoneId")
                .isNotNull();
        assertThat(participantMilestoneRepository.exists(studyId, participant.getParticipantId(), milestone.getMilestoneId())).isTrue();

        Instant updatedDateTime = Instant.parse("2024-06-20T09:00:00Z");
        ParticipantMilestone updated = participantMilestoneRepository.update(new ParticipantMilestone()
                .setStudyId(studyId)
                .setParticipantId(participant.getParticipantId())
                .setMilestoneId(milestone.getMilestoneId())
                .setDateTime(updatedDateTime));
        assertThat(updated.getDateTime()).isEqualTo(updatedDateTime);
        assertThat(updated.getModified()).isAfter(updated.getCreated());
        assertThat(updated.getParticipantMilestoneId())
                .as("participantMilestoneId is stable across updates")
                .isEqualTo(inserted.getParticipantMilestoneId());

        participantMilestoneRepository.deleteByIds(studyId, participant.getParticipantId(), milestone.getMilestoneId());
        assertThat(participantMilestoneRepository.exists(studyId, participant.getParticipantId(), milestone.getMilestoneId())).isFalse();
    }

    @Test
    @DisplayName("A participant's milestones are listed in the same order as the study's milestones, not insertion order")
    void testParticipantMilestonesAreOrderedLikeMilestones() {
        Long studyId = studyRepository.insert(new Study().setContact(new Contact().setPerson("test").setEmail("test"))).getStudyId();
        Milestone baseline = milestoneRepository.insert(new Milestone().setStudyId(studyId).setName("Baseline"));
        Milestone followUp = milestoneRepository.insert(new Milestone().setStudyId(studyId).setName("Follow-up"));
        Milestone end = milestoneRepository.insert(new Milestone().setStudyId(studyId).setName("End"));
        Participant participant = participantRepository.insert(new Participant()
                .setStudyId(studyId).setAlias("P1").setRegistrationToken("rt_p1"));

        // set them in reverse order of the milestones' orderIndex
        participantMilestoneRepository.insert(new ParticipantMilestone()
                .setStudyId(studyId).setParticipantId(participant.getParticipantId())
                .setMilestoneId(end.getMilestoneId()).setDateTime(Instant.parse("2024-08-01T09:00:00Z")));
        participantMilestoneRepository.insert(new ParticipantMilestone()
                .setStudyId(studyId).setParticipantId(participant.getParticipantId())
                .setMilestoneId(baseline.getMilestoneId()).setDateTime(Instant.parse("2024-06-01T09:00:00Z")));
        participantMilestoneRepository.insert(new ParticipantMilestone()
                .setStudyId(studyId).setParticipantId(participant.getParticipantId())
                .setMilestoneId(followUp.getMilestoneId()).setDateTime(Instant.parse("2024-07-01T09:00:00Z")));

        List<ParticipantMilestone> listed = participantMilestoneRepository.listByParticipant(studyId, participant.getParticipantId());

        assertThat(listed).extracting(ParticipantMilestone::getMilestoneId)
                .containsExactly(baseline.getMilestoneId(), followUp.getMilestoneId(), end.getMilestoneId());
        assertThat(listed).extracting(ParticipantMilestone::getName)
                .containsExactly("Baseline", "Follow-up", "End");
        assertThat(listed).extracting(ParticipantMilestone::getParticipantMilestoneId)
                .as("participantMilestoneId is its own generated, unique id per study")
                .doesNotContainNull()
                .doesNotHaveDuplicates();
    }
}
