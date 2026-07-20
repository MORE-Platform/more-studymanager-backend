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
import io.redlink.more.studymanager.core.properties.ActionProperties;
import io.redlink.more.studymanager.model.Action;
import io.redlink.more.studymanager.model.audit.AuditLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@EnableAutoConfiguration
@ContextConfiguration(classes = {AuditLogRepository.class, JPAConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test-containers-flyway")
class AuditLogRepositoryTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void deleteAll() {
        auditLogRepository.clear();
    }

    @Test
    @DisplayName("Create AuditLog")
    void testInsert() {
        String userId = "test-user";
        String userName = "Test User";
        Long studyId = 1L;
        String actionId = "test-action";
        Instant timestamp = Instant.now().minusSeconds(10).truncatedTo(ChronoUnit.SECONDS);
        Action detailsAction = new Action()
                .setType("test")
                .setProperties(new ActionProperties(Map.of("test","dummy")))
                .setCreated(timestamp.minusSeconds(5))
                .setModified(timestamp);

        Instant detailsTimestamp = timestamp.minusSeconds(10);
        Map<String,Object> details = Map.of(
                "user_roles", Arrays.asList("test-role1", "test-role2"),
                "detail_state", Boolean.TRUE,
                "detail_integer", 42,
                "detail_number", 3.1415,
                "detail_text", "This is an important test",
                "detail_bean", detailsAction,
                "details_timestamp", detailsTimestamp);

        AuditLog auditLog = new AuditLog(
                userId,
                studyId,
                actionId,
                timestamp)
                .setDetails(details)
                .setActionState(AuditLog.ActionState.success)
                .setUserName(userName);

        AuditLog auditLogResonse = auditLogRepository.insert(auditLog);

        assertThat(auditLogResonse).isNotNull();
        //assert that the ID and the created timestamp are initialised by the DB
        assertThat(auditLogResonse.getId()).isNotNull();
        assertThat(auditLogResonse.getCreated()).isNotNull();
        assertThat(auditLogResonse.getCreated()).isBetween(timestamp, Instant.now().plusMillis(1));
        //assert the values of the AuditLog
        assertThat(auditLogResonse.getUserId()).isEqualTo(userId);
        assertThat(auditLogResonse.getUserName()).isEqualTo(userName);
        assertThat(auditLogResonse.getStudyId()).isEqualTo(studyId);
        assertThat(auditLogResonse.getAction()).isEqualTo(actionId);
        assertThat(auditLogResonse.getTimestamp()).isEqualTo(timestamp);
        assertThat(auditLogResonse.getActionState()).isEqualTo(AuditLog.ActionState.success);
        //assert Details
        assertThat(auditLogResonse.getDetails()).isNotNull();
        assertThat(auditLogResonse.getDetails().keySet()).isEqualTo(details.keySet());
        assertThat(auditLogResonse.getDetails().get("user_roles")).isEqualTo(Arrays.asList("test-role1", "test-role2"));
        assertThat(auditLogResonse.getDetails().get("detail_state")).isEqualTo(Boolean.TRUE);
        assertThat(auditLogResonse.getDetails().get("detail_integer")).isEqualTo(42);
        assertThat(auditLogResonse.getDetails().get("detail_number")).isEqualTo(3.1415);
        assertThat(auditLogResonse.getDetails().get("detail_text")).isEqualTo("This is an important test");
        //NOTE: Date/Time and Instant values are written as UTC date/time strings
        assertThat(auditLogResonse.getDetails().get("details_timestamp")).isEqualTo(detailsTimestamp.toString());

        //Validate Details
        //NOTE: class information is lost. So the returned AuditLog will contain the information as Map<String,Object>
        assertThat(auditLogResonse.getDetails().get("detail_bean")).isInstanceOf(Map.class);
        Map<String,Object> detailsBean = (Map<String,Object>) auditLogResonse.getDetails().get("detail_bean");
        assertThat(detailsBean.get("type")).isEqualTo("test");
        assertThat(detailsBean.get("properties")).isEqualTo(Map.of("test","dummy"));
        //NOTE: Date/Time and Instant values are written as UTC date/time strings
        assertThat(detailsBean.get("created")).isEqualTo(detailsAction.getCreated().toString());
        assertThat(detailsBean.get("modified")).isEqualTo(detailsAction.getModified().toString());
    }

    @Test
    @DisplayName("List AuditLogs and get Metadata")
    void testListAndCountAuditLogs() {
        Long studyId = 1L;

        Action detailsAction = new Action()
                .setType("test")
                .setProperties(new ActionProperties(Map.of("test","dummy")))
                .setCreated(Instant.now().minusSeconds(60))
                .setModified(Instant.now());

        Map<String,Object> details = Map.of(
                "detail_state", Boolean.TRUE,
                "detail_integer", 42,
                "detail_number", 3.1415,
                "detail_text", "This is an important test",
                "detail_bean", detailsAction,
                "details_timestamp", Instant.now().minusSeconds(70));

        AuditLog auditLog = new AuditLog(
                "test-user",
                studyId,
                "test-action",
                Instant.now().minusSeconds(50).truncatedTo(ChronoUnit.SECONDS))
                .setDetails(details)
                .setActionState(AuditLog.ActionState.success);

        AuditLog auditLog2 = new AuditLog(
                "test-user",
                studyId,
                "test-action",
                Instant.now().minusSeconds(10).truncatedTo(ChronoUnit.SECONDS))
                .setDetails(details)
                .setActionState(AuditLog.ActionState.success);


        auditLogRepository.insert(auditLog);
        AuditLog auditLogResonse = auditLogRepository.insert(auditLog2);

        Stream<AuditLog> auditLogs = auditLogRepository.listAuditLog(studyId);

        assertThat(auditLogResonse).isNotNull();
        assertThat(auditLogs).isNotNull();
        assertThat(auditLogs).hasSize(2);

        Long count = auditLogRepository.countAuditLogEntries(studyId);
        assertThat(count).isEqualTo(2);
    }
}
