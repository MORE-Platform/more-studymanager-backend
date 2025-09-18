/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.core.properties.ActionProperties;
import io.redlink.more.studymanager.model.*;
import io.redlink.more.studymanager.model.audit.AuditLog;
import io.redlink.more.studymanager.repository.AuditLogRepository;
import io.redlink.more.studymanager.service.AuditService;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@WebMvcTest({AuditLogAPIV1Controller.class})
@AutoConfigureMockMvc(addFilters = false)
class AuditLogControllerTest {

    @MockitoBean
    AuditService auditService;

    @MockitoBean
    private AuditLogRepository auditLogRepository;

    @MockitoBean
    OAuth2AuthenticationService oAuth2AuthenticationService;

    @BeforeEach
    void setUp() {
        when(oAuth2AuthenticationService.getCurrentUser()).thenReturn(
                new AuthenticatedUser(
                        UUID.randomUUID().toString(),
                        "Test User", "test@example.com", "Test Inc.",
                        EnumSet.allOf(PlatformRole.class)
                )
        );
        auditLogRepository.deleteAllAuditLogs();
    }

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("Get Auditlog Metadata for a Auditlog")
    void testGetAuditlogMetadata() throws Exception {
        Long studyId = 1L;

        when(auditLogRepository.countAuditLogEntries(Mockito.any(Long.class))).thenReturn(2L);
        when(auditService.countAuditLogEntries(Mockito.any(Long.class))).thenReturn(2L);

        mvc.perform(get("/api/v1/auditlog/study/{studyId}", studyId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studyId").value(1))
                .andExpect(jsonPath("$.length").value(2));
    }

    @Test
    @DisplayName("Export Auditlogs via streams")
    void testExportAuditLogs() throws Exception {
        Long studyId = 1L;

        Action detailsAction = new Action()
                .setType("test")
                .setProperties(new ActionProperties(Map.of("test","dummy")))
                .setCreated(Instant.now().minusSeconds(60))
                .setModified(Instant.now());

        Map<String,Object> details = Map.of(
                "user_roles", List.of("admin", "viewer"),
                "detail_state", Boolean.TRUE,
                "detail_integer", 42,
                "detail_number", 3.1415,
                "detail_text", "This is an important test",
                "detail_bean", detailsAction,
                "details_timestamp", Instant.now().minusSeconds(70));

        AuditLog auditLog1 = new AuditLog(
                42L,
                Instant.now().truncatedTo(ChronoUnit.SECONDS),
                "test-user1",
                studyId,
                "test-action1",
                Instant.now().minusSeconds(10).truncatedTo(ChronoUnit.SECONDS))
                .setDetails(details)
                .setActionState(AuditLog.ActionState.success)
                .setUserName("Test User1");

        AuditLog auditLog2 = new AuditLog(
                69L,
                Instant.now().truncatedTo(ChronoUnit.SECONDS),
                "test-user2",
                studyId,
                "test-action2",
                Instant.now().minusSeconds(50).truncatedTo(ChronoUnit.SECONDS))
                .setDetails(details)
                .setActionState(AuditLog.ActionState.success)
                .setUserName("Test User2");

        when(auditLogRepository.listAuditLog(anyLong()))
                .thenAnswer(invocation -> Stream.of(auditLog1, auditLog2));
        when(auditService.getAuditLogs(anyLong()))
                .thenAnswer(invocation -> Stream.of(auditLog1, auditLog2));
        when(auditService.listAuditLog(Mockito.any(Long.class))).thenReturn(Arrays.asList(auditLog1, auditLog2));

        mvc.perform(get("/api/v1/auditlog/study/{studyId}/export", studyId))
                .andExpect(request().asyncStarted())
                .andDo(MvcResult::getAsyncResult)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(42))
                .andExpect(jsonPath("$[0].studyId").value(studyId))
                .andExpect(jsonPath("$[0].userId").value("test-user1"))
                .andExpect(jsonPath("$[0].userName").value("Test User1"))
                .andExpect(jsonPath("$[0].action").value("test-action1"))
                .andExpect(jsonPath("$[0].actionState").value("success"))
                .andExpect(jsonPath("$[0].detail_state").value(Boolean.TRUE))
                .andExpect(jsonPath("$[0].detail_text").value("This is an important test"))
                .andExpect(jsonPath("$[0].userRoles[0]").value("admin"))
                .andExpect(jsonPath("$[0].userRoles[1]").value("viewer"))
                .andExpect(jsonPath("$[0].created").exists());
    }

}
