/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.AuditLogMetadataDTO;
import io.redlink.more.studymanager.api.v1.webservices.AuditlogApi;
import io.redlink.more.studymanager.controller.RequiresStudyRole;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.audit.AuditLog;
import io.redlink.more.studymanager.model.data.AuditlogMetadata;
import io.redlink.more.studymanager.model.transformer.AuditlogTransformer;
import io.redlink.more.studymanager.properties.GatewayProperties;
import io.redlink.more.studymanager.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@EnableConfigurationProperties(GatewayProperties.class)
public class AuditLogAPIV1Controller implements AuditlogApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogAPIV1Controller.class);

    private final AuditService service;
    private final GatewayProperties properties;

    public AuditLogAPIV1Controller(AuditService service, GatewayProperties properties) {
        this.service = service;
        this.properties = properties;
    }

    @RequiresStudyRole({StudyRole.STUDY_ADMIN})
    public ResponseEntity<AuditLogMetadataDTO> getAuditlogMetadata(
            Long studyId) {
        long count = service.countAuditlogEntries(studyId);

        AuditlogMetadata metadata = new AuditlogMetadata(
                studyId,
                count
        );

        return ResponseEntity.ok(
                AuditlogTransformer.toAuditlogMetadataDTO_V1(metadata)
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN})
    public ResponseEntity<StreamingResponseBody> exportAuditlog(Long studyId) {
        List<AuditLog> dtos = service.listAuditlog(studyId);

        if (dtos.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders
                    .set("Content-Disposition", "attachment; filename=auditlogs_" + studyId + ".json");

            return ResponseEntity
                    .ok()
                    .headers(responseHeaders)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(outputStream -> {
                        try {
                            service.exportAuditlog(outputStream, studyId);
                        } catch (Exception e) {
                            LOGGER.warn("Error exporting study data for study_{}: {}", studyId, e.getMessage(), e);
                        }
                    });
        }
    }
}
