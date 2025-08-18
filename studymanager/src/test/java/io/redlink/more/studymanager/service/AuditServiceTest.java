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
import io.redlink.more.studymanager.model.*;
import io.redlink.more.studymanager.model.audit.AuditLog;
import io.redlink.more.studymanager.properties.AuditProperties;
import io.redlink.more.studymanager.repository.AuditLogRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    AuditLogRepository auditLogRepository;

    @Mock
    StudyStateService studyStateService;

    @Mock
    AuditProperties auditProperties;

    @InjectMocks
    AuditService auditService;

    private final AuthenticatedUser currentUser = new AuthenticatedUser(
            UUID.randomUUID().toString(),
            "Test User", "test@example.com", "Test Inc.",
            EnumSet.allOf(PlatformRole.class)
    );

    @Test
    @DisplayName("When the auditLog is saved it should return the study with id and created date.")
    void testRecord() {
        AuditLog auditLog = new AuditLog(
                currentUser.id(),
                1L,
                "test-action",
                Instant.now().minusSeconds(10))
                .setActionState(AuditLog.ActionState.success)
                .setDetails(Map.of("test","dummy"));

        when(auditLogRepository.insert(any(AuditLog.class)))
                .thenAnswer(invocationOnMock -> {
                    var al = (AuditLog) invocationOnMock.getArguments()[0];
                    return new AuditLog(1L,Instant.now(), al.getUserId(), al.getStudyId(), al.getAction(),al.getTimestamp())
                            .setActionState(al.getActionState())
                            .setDetails(al.getDetails())
                            .setResource(al.getResource());
                });
        //when(studyStateService.hasStudyState(any(), any())).thenReturn(true);

        //when(auditProperties.detailsByteLimit()).thenReturn(-1L);
        when(auditProperties.studyStates()).thenReturn(new LinkedList<>());

        Optional<AuditLog> auditLogResponse = auditService.record(auditLog);

        assertThat(auditLogResponse).isPresent();
        assertThat(auditLogResponse.get().getId()).isEqualTo(1L);
        assertThat(auditLogResponse.get().getId()).isNotNull();


    }

    @Test
    @DisplayName("When the auditLog is not saved for a study in a state that does not require auditing")
    void testSkipRecord() {
        AuditLog auditLog = new AuditLog(
                currentUser.id(),
                1L,
                "test-action",
                Instant.now().minusSeconds(10))
                .setActionState(AuditLog.ActionState.success)
                .setDetails(Map.of("test","dummy"));

        when(auditLogRepository.insert(any(AuditLog.class)))
                .thenAnswer(invocationOnMock -> {
                    var al = (AuditLog) invocationOnMock.getArguments()[0];
                    return new AuditLog(1L,Instant.now(), al.getUserId(), al.getStudyId(), al.getAction(),al.getTimestamp())
                            .setActionState(al.getActionState())
                            .setDetails(al.getDetails())
                            .setResource(al.getResource());
                });
        Collection<Study.Status> activeStudyStates = EnumSet.of(Study.Status.PAUSED,Study.Status.ACTIVE);
        //test a not auditing study statefirst
        when(studyStateService.hasStudyState(any(), eq(activeStudyStates))).thenReturn(false);

        when(auditProperties.studyStates()).thenReturn(activeStudyStates);
        Optional<AuditLog> auditLogResponse = auditService.record(auditLog);
        assertThat(auditLogResponse).isNotPresent();

        //test a auditing study state second
        when(studyStateService.hasStudyState(any(), eq(activeStudyStates))).thenReturn(true);
        Optional<AuditLog> auditLogResponse2 = auditService.record(auditLog);
        assertThat(auditLogResponse2).isPresent();

    }

}
