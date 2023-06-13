package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.model.*;
import io.redlink.more.studymanager.repository.StudyAclRepository;
import io.redlink.more.studymanager.repository.StudyRepository;
import io.redlink.more.studymanager.repository.UserRepository;
import java.time.Instant;
import java.util.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudyServiceTest {

    @Mock
    StudyRepository studyRepository;

    @Mock
    StudyAclRepository studyAclRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    StudyPermissionService studyPermissionService;

    @Mock
    StudyStateService studyStateService;

    @InjectMocks
    StudyService studyService;

    private final AuthenticatedUser currentUser = new AuthenticatedUser(
            UUID.randomUUID().toString(),
            "Test User", "test@example.com", "Test Inc.",
            EnumSet.allOf(PlatformRole.class)
    );

    @Test
    @DisplayName("When the study is saved it should return the study with id.")
    void testSaveStudy() {
        Study study = new Study();
        study.setTitle("test study")
                .setContact(new Contact().setPerson("testPerson").setEmail("testMail"));

        when(studyRepository.insert(any(Study.class)))
                .thenAnswer(invocationOnMock -> new Study().setStudyId(1L).setTitle(study.getTitle())
                        .setContact(((Study) invocationOnMock.getArgument(0)).getContact()));
        when(userRepository.save(any(User.class)))
                .thenAnswer(i -> {
                    var u = i.getArgument(0, User.class);
                    return new MoreUser(u.id(), u.fullName(), u.institution(), u.email(), Instant.now(), Instant.now());
                });
        when(studyRepository.getById(eq(1L), any(User.class)))
                .thenAnswer(invocationOnMock ->
                        Optional.of(new Study().setStudyId(1L).setTitle(study.getTitle())
                                .setContact(study.getContact())));

        Study studyResponse = studyService.createStudy(study, currentUser);

        assertThat(studyResponse.getStudyId()).isEqualTo(1L);
        assertThat(studyResponse.getTitle()).isSameAs(study.getTitle());
        assertThat(studyResponse.getContact().getPerson()).isEqualTo("testPerson");
        assertThat(studyResponse.getContact().getEmail()).isEqualTo("testMail");

        verify(studyAclRepository, times(1).description("Initial ACL should be set"))
                .setRoles(studyResponse.getStudyId(), currentUser.id(), EnumSet.allOf(StudyRole.class), null);
    }

    @Test
    void testListStudies() {
        when(studyRepository.listStudiesByAclOrderByModifiedDesc(any(), any()))
                .thenAnswer(i -> List.of(new Study()
                        .setContact(new Contact()
                                .setPerson(Base64.getEncoder().encodeToString("test person".getBytes()))
                                .setEmail(Base64.getEncoder().encodeToString("test@mail.tst".getBytes())))));

        assertThat(studyService.listStudies(currentUser, EnumSet.of(StudyRole.STUDY_VIEWER))).isNotEmpty();
        verify(studyRepository, times(1))
                .listStudiesByAclOrderByModifiedDesc(currentUser, EnumSet.of(StudyRole.STUDY_VIEWER));


        assertThat(studyService.listStudies(currentUser)).isNotEmpty();
        verify(studyRepository, times(1))
                .listStudiesByAclOrderByModifiedDesc(currentUser, EnumSet.allOf(StudyRole.class));


    }

    @Test
    @DisplayName("When the study state is set incorrect it should fail")
    void testSetStatus() {
        testForbiddenSetStatus(Study.Status.DRAFT, Study.Status.DRAFT);
        testForbiddenSetStatus(Study.Status.CLOSED, Study.Status.DRAFT);
    }

    private void testForbiddenSetStatus(Study.Status statusBefore, Study.Status statusAfter) {
        Study study = new Study().setStudyId(1L).setStudyState(statusBefore)
                .setContact(new Contact()
                        .setPerson(Base64.getEncoder().encodeToString("test person".getBytes()))
                        .setEmail(Base64.getEncoder().encodeToString("test@mail.tst".getBytes()))
                );
        when(studyRepository.getById(any(Long.class), any())).thenReturn(Optional.of(study));
        Assertions.assertThrows(BadRequestException.class,
                () -> studyService.setStatus(1L, statusAfter, currentUser));
    }
}
