package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.MoreUser;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.StudyRole;
import java.util.EnumSet;
import java.util.Set;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test-containers-flyway")
class StudyAclRepositoryTest {

    @Autowired
    private StudyRepository studyRepository;
    @Autowired
    private StudyAclRepository studyAclRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MoreUser user1, user2;
    private Study study;

    @BeforeEach
    void deleteAll() {
        jdbcTemplate.update("DELETE FROM study_acl");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM studies");

        user1 = userRepository.save(new MoreUser("user1", "User One", null, null));
        user2 = userRepository.save(new MoreUser("user2", "User Two", null, null));

        study = studyRepository.insert(new Study().setTitle("ACL-Test"));
    }



    @Test
    void testSetRoles() {
        final Set<StudyRole> assignedRoles = studyAclRepository.setRoles(study.getStudyId(), user1.id(), StudyRole.STUDY_ADMIN);
        assertThat(assignedRoles, Matchers.containsInAnyOrder(StudyRole.STUDY_ADMIN));

        assertTrue(studyAclRepository.hasRole(study.getStudyId(), user1.id(), StudyRole.STUDY_ADMIN));
        assertFalse(studyAclRepository.hasRole(study.getStudyId(), user1.id(), StudyRole.STUDY_OPERATOR));
        assertFalse(studyAclRepository.hasRole(study.getStudyId(), user1.id(), StudyRole.STUDY_VIEWER));

        assertFalse(studyAclRepository.hasAnyRole(study.getStudyId(), user1.id(), StudyRole.STUDY_VIEWER, StudyRole.STUDY_OPERATOR));

        final Set<StudyRole> reassignedRoles = studyAclRepository.setRoles(study.getStudyId(), user1.id(), StudyRole.STUDY_VIEWER, StudyRole.STUDY_OPERATOR);
        assertThat(reassignedRoles, Matchers.containsInAnyOrder(StudyRole.STUDY_VIEWER, StudyRole.STUDY_OPERATOR));

        assertFalse(studyAclRepository.hasRole(study.getStudyId(), user1.id(), StudyRole.STUDY_ADMIN));
        assertTrue(studyAclRepository.hasRole(study.getStudyId(), user1.id(), StudyRole.STUDY_OPERATOR));
        assertTrue(studyAclRepository.hasRole(study.getStudyId(), user1.id(), StudyRole.STUDY_VIEWER));

        assertTrue(studyAclRepository.hasAllRoles(study.getStudyId(), user1.id(), StudyRole.STUDY_VIEWER, StudyRole.STUDY_OPERATOR));
        assertFalse(studyAclRepository.hasAllRoles(study.getStudyId(), user1.id(), StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR));
        assertTrue(studyAclRepository.hasAnyRole(study.getStudyId(), user1.id(), StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR));
    }

    @Test
    void testSetAndGetRoles() {
        var rolesUser1 = EnumSet.of(StudyRole.STUDY_OPERATOR, StudyRole.STUDY_VIEWER);
        var rolesUser2 = EnumSet.of(StudyRole.STUDY_ADMIN, StudyRole.STUDY_VIEWER);
        
        assertThat(studyAclRepository.setRoles(study.getStudyId(), user1.id(), rolesUser1), Matchers.equalTo(rolesUser1));
        assertThat(studyAclRepository.getRoles(study.getStudyId(), user1.id()), Matchers.equalTo(rolesUser1));

        assertThat(studyAclRepository.setRoles(study.getStudyId(), user2.id(), rolesUser2), Matchers.equalTo(rolesUser2));
        assertThat(studyAclRepository.getRoles(study.getStudyId(), user2.id()), Matchers.equalTo(rolesUser2));

        var acl = studyAclRepository.getACL(study);
        assertThat(acl, Matchers.aMapWithSize(2));
        assertThat(acl.get(user1), Matchers.equalTo(rolesUser1));
        assertThat(acl.get(user2), Matchers.equalTo(rolesUser2));

        studyAclRepository.clearRoles(study.getStudyId(), user1.id());
        assertFalse(studyAclRepository.hasAnyRole(study.getStudyId(), user1.id(), EnumSet.allOf(StudyRole.class)));
    }
}