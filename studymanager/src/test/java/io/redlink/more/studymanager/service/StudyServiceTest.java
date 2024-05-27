/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.model.AuthenticatedUser;
import io.redlink.more.studymanager.model.Contact;
import io.redlink.more.studymanager.model.MoreUser;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.PlatformRole;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.User;
import io.redlink.more.studymanager.repository.StudyAclRepository;
import io.redlink.more.studymanager.repository.StudyRepository;
import io.redlink.more.studymanager.repository.UserRepository;
import java.time.Instant;
import java.util.Base64;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudyServiceTest {

    @Mock
    StudyRepository studyRepository;

    @Mock
    ParticipantService participantService;

    @Mock
    ObservationService observationService;

    @Mock
    InterventionService interventionService;

    @Mock
    IntegrationService integrationService;

    @Mock
    PushNotificationService pushNotificationService;

    @Mock
    ElasticService elasticService;

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
        testForbiddenSetStatus(Study.Status.PAUSED, Study.Status.DRAFT);
        testForbiddenSetStatus(Study.Status.PAUSED, Study.Status.CLOSED);
        testForbiddenSetStatus(Study.Status.PREVIEW, Study.Status.CLOSED);
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

    @Test
    void testWorkflowSideEffects() {
        final Study study = new Study().setStudyId(1L)
                .setContact(new Contact().setPerson("testPerson").setEmail("testMail"));
        final List<Participant> pt = List.of(
                new Participant().setParticipantId(1).setStudyId(study.getStudyId()),
                new Participant().setParticipantId(2).setStudyId(study.getStudyId())
        );

        when(studyRepository.getById(eq(study.getStudyId()), any())).thenReturn(Optional.of(study));
        when(participantService.listParticipants(study.getStudyId())).thenReturn(pt);
        when(studyRepository.setStateById(eq(study.getStudyId()), any()))
                .thenAnswer(i -> Optional.of(study.setStudyState(i.getArgument(1))));

        StudyService.VALID_STUDY_TRANSITIONS.forEach((from, tos) -> {
            tos.forEach(to -> {
                study.setStudyState(from);
                clearInvocations(
                        observationService, interventionService, integrationService,
                        participantService, pushNotificationService, elasticService
                );

                studyService.setStatus(1L, to, currentUser);

                verify(observationService,
                        times(1).description("%s -> %s should align observations".formatted(from, to))
                ).alignObservationsWithStudyState(study);
                verify(interventionService,
                        times(1).description("%s -> %s should align interventions".formatted(from, to))
                ).alignInterventionsWithStudyState(study);
                verify(integrationService,
                        times(1).description("%s -> %s should align integrations".formatted(from, to))
                ).alignIntegrationsWithStudyState(study);
                verify(participantService,
                        times(1).description("%s -> %s should align participants".formatted(from, to))
                ).alignParticipantsWithStudyState(study);
                verify(pushNotificationService,
                        times(pt.size()).description("%s -> %s should send %d notifications".formatted(from, to, pt.size()))
                ).sendPushNotification(eq(study.getStudyId()), anyInt(), any(), any(), any());

                // ONLY when transitioning to back to DRAFT, clear the collected data in Elastic
                if ((from == Study.Status.PREVIEW || from == Study.Status.PAUSED_PREVIEW)
                        && to == Study.Status.DRAFT) {
                    verify(elasticService,
                            times(1).description("%s -> %s should delete the elastic index".formatted(from, to))
                    ).deleteIndex(study.getStudyId());
                } else {
                    verify(elasticService,
                            never().description("%s -> %s must not delete the elastic index".formatted(from, to))
                    ).deleteIndex(study.getStudyId());
                }
            });

            clearInvocations(
                    observationService, interventionService, integrationService,
                    participantService, pushNotificationService, elasticService
            );
            EnumSet.complementOf(EnumSet.copyOf(tos)).forEach(invalidTo -> {
                study.setStudyState(from);
                Assertions.assertThrows(BadRequestException.class,
                        () -> studyService.setStatus(1L, invalidTo, currentUser),
                        () -> "Invalid Transition: %s -> %s".formatted(from, invalidTo));
                Mockito.verifyNoInteractions(
                        observationService, interventionService, integrationService,
                        participantService, pushNotificationService, elasticService
                );
            });

        });
    }
}
