package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.exception.DataConstraintException;
import io.redlink.more.studymanager.model.*;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    private MoreUser admin, user1, user2;
    private Study study;

    @BeforeEach
    void deleteAll() {
        jdbcTemplate.update("DELETE FROM study_acl");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM studies");

        admin = userRepository.save(new MoreUser("admin", "Admin", null, null));
        user1 = userRepository.save(new MoreUser("user1", "User One", null, null));
        user2 = userRepository.save(new MoreUser("user2", "User Two", null, null));

        study = studyRepository.insert(new Study().setContact(new Contact().setPerson("test").setEmail("test")).setTitle("ACL-Test"));
        studyAclRepository.setRoles(study.getStudyId(), admin.id(), null, StudyRole.STUDY_ADMIN);
    }


    @Test
    void testSetRoles() {
        final Set<StudyRole> assignedRoles = studyAclRepository.setRoles(study.getStudyId(), user1.id(), null, StudyRole.STUDY_ADMIN);
        assertThat(assignedRoles, Matchers.containsInAnyOrder(StudyRole.STUDY_ADMIN));

        assertTrue(studyAclRepository.hasRole(study.getStudyId(), user1.id(), StudyRole.STUDY_ADMIN));
        assertFalse(studyAclRepository.hasRole(study.getStudyId(), user1.id(), StudyRole.STUDY_OPERATOR));
        assertFalse(studyAclRepository.hasRole(study.getStudyId(), user1.id(), StudyRole.STUDY_VIEWER));

        assertFalse(studyAclRepository.hasAnyRole(study.getStudyId(), user1.id(), StudyRole.STUDY_VIEWER, StudyRole.STUDY_OPERATOR));

        final Set<StudyRole> reassignedRoles = studyAclRepository.setRoles(study.getStudyId(), user1.id(), null, StudyRole.STUDY_VIEWER, StudyRole.STUDY_OPERATOR);
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

        assertThat(studyAclRepository.setRoles(study.getStudyId(), user1.id(), rolesUser1, null), Matchers.equalTo(rolesUser1));
        assertThat(studyAclRepository.getRoles(study.getStudyId(), user1.id()), Matchers.equalTo(rolesUser1));

        assertThat(studyAclRepository.setRoles(study.getStudyId(), user2.id(), rolesUser2, null), Matchers.equalTo(rolesUser2));
        assertThat(studyAclRepository.getRoles(study.getStudyId(), user2.id()), Matchers.equalTo(rolesUser2));

        var acl = studyAclRepository.getACL(study);
        assertThat(acl, Matchers.aMapWithSize(3));
        assertThat(acl.get(admin), Matchers.equalTo(EnumSet.of(StudyRole.STUDY_ADMIN)));
        assertThat(acl.get(user1), Matchers.equalTo(rolesUser1));
        assertThat(acl.get(user2), Matchers.equalTo(rolesUser2));

        studyAclRepository.clearRoles(study.getStudyId(), user1.id());
        assertFalse(studyAclRepository.hasAnyRole(study.getStudyId(), user1.id(), EnumSet.allOf(StudyRole.class)));
    }

    @Test
    void testListStudiesByACL() {
        // user1 has study
        studyAclRepository.setRoles(study.getStudyId(), user1.id(), EnumSet.allOf(StudyRole.class), null);

        // second study is for user 2
        var study2 = studyRepository.insert(new Study().setContact(new Contact().setPerson("test").setEmail("test")).setTitle("Study 2"));
        studyAclRepository.setRoles(study2.getStudyId(), user2.id(), EnumSet.allOf(StudyRole.class), null);
        // user2 has view-rights on study1
        studyAclRepository.setRoles(study.getStudyId(), user2.id(), EnumSet.of(StudyRole.STUDY_VIEWER), null);


        assertThat(
                "<user1> has only access to <study>",
                studyRepository.listStudiesByAclOrderByModifiedDesc(user1, EnumSet.allOf(StudyRole.class)),
                Matchers.contains(hasSameStudyId(study))
        );
        assertThat(
                "<user2> has access to both studies",
                studyRepository.listStudiesByAclOrderByModifiedDesc(user2, EnumSet.allOf(StudyRole.class)),
                Matchers.contains(hasSameStudyId(study2), hasSameStudyId(study))
        );
        assertThat(
                "<user2> has VIEWER-access to both studies",
                studyRepository.listStudiesByAclOrderByModifiedDesc(user2, EnumSet.of(StudyRole.STUDY_VIEWER)),
                Matchers.contains(hasSameStudyId(study2), hasSameStudyId(study))
        );
        assertThat(
                "<user2> has ADMIN-access only to <study2>",
                studyRepository.listStudiesByAclOrderByModifiedDesc(user2, EnumSet.of(StudyRole.STUDY_ADMIN)),
                Matchers.contains(hasSameStudyId(study2))
        );

    }

    @Test
    void testAclDetails() {
        studyAclRepository.setRoles(study.getStudyId(), user1.id(), EnumSet.of(StudyRole.STUDY_VIEWER), user2.id());
        studyAclRepository.setRoles(study.getStudyId(), user1.id(), EnumSet.of(StudyRole.STUDY_VIEWER, StudyRole.STUDY_OPERATOR), user1.id());

        var roleDetails = studyAclRepository.getRoleDetails(study.getStudyId(), user1.id());
        assertThat("two roles expected", roleDetails, Matchers.hasSize(2));
        assertThat("VIEWER was set by user2",
                roleDetails.stream().filter(d -> d.role() == StudyRole.STUDY_VIEWER).findFirst(),
                valueMatches(hasRoleCreator(user2.id()))
        );
        assertThat("OPERATOR was set by user1",
                roleDetails.stream().filter(d -> d.role() == StudyRole.STUDY_OPERATOR).findFirst(),
                valueMatches(hasRoleCreator(user1.id()))
        );
    }

    @Test
    @DisplayName("Ensure that there is at least on STUDY_ADMIN")
    void testAtLeastOneStudyAdmin() {

        assertThrows(DataConstraintException.class,
                () -> studyAclRepository.clearRoles(study.getStudyId(), admin.id()),
                "Removing the last ADMIN is forbidden");
        assertThrows(DataConstraintException.class,
                () -> studyAclRepository.setRoles(study.getStudyId(), admin.id(), user1.id()),
                "Removing the last ADMIN is forbidden");

        studyAclRepository.setRoles(study.getStudyId(), user1.id(), admin.id(), StudyRole.STUDY_ADMIN);
        studyAclRepository.clearRoles(study.getStudyId(), admin.id());
    }

    static TypeSafeDiagnosingMatcher<Study> hasSameStudyId(Study study) {
        return hasStudyId(study.getStudyId());
    }

    static TypeSafeDiagnosingMatcher<Study> hasStudyId(long studyId) {
        return new TypeSafeDiagnosingMatcher<>() {
            @Override
            protected boolean matchesSafely(Study item, Description mismatchDescription) {
                describeTo(mismatchDescription, item.getStudyId());
                return studyId == item.getStudyId();
            }

            @Override
            public void describeTo(Description description) {
                describeTo(description, studyId);
            }

            private void describeTo(Description description, Long studyId) {
                description.appendText("a study with studyId=").appendValue(studyId);
            }
        };
    }

    static TypeSafeDiagnosingMatcher<StudyUserRoles.StudyRoleDetails> hasRoleCreator(String userId) {
        return new TypeSafeDiagnosingMatcher<>() {
            @Override
            protected boolean matchesSafely(StudyUserRoles.StudyRoleDetails item, Description mismatchDescription) {
                describeTo(mismatchDescription, item.creator().id());
                return userId.equals(item.creator().id());
            }

            @Override
            public void describeTo(Description description) {
                describeTo(description, userId);
            }

            private static void describeTo(Description description, String userId) {
                description.appendText("role-details created by ").appendValue(userId);
            }
        };
    }

    static <T> TypeSafeDiagnosingMatcher<Optional<T>> valueMatches(Matcher<T> matcher) {
        return new TypeSafeDiagnosingMatcher<>() {
            @Override
            protected boolean matchesSafely(Optional<T> item, Description mismatchDescription) {
                if (item.isEmpty()) {
                    mismatchDescription.appendText("Empty Optional");
                } else {
                    matcher.describeMismatch(item.get(),
                            mismatchDescription.appendText("Optional with value ")
                    );
                }

                return item.isPresent() && matcher.matches(item.get());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Optional with value ").appendDescriptionOf(matcher);
            }
        };
    }

}