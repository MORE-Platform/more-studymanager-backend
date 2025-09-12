/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.controller.studymanager;

import com.fasterxml.jackson.core.JsonEncoding;
import io.redlink.more.studymanager.api.v1.model.AuditLogMetadataDTO;
import io.redlink.more.studymanager.api.v1.webservices.AuditLogApi;
import io.redlink.more.studymanager.controller.RequiresStudyRole;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.audit.AuditLog;
import io.redlink.more.studymanager.model.data.AuditLogMetadata;
import io.redlink.more.studymanager.model.transformer.AuditlogTransformer;
import io.redlink.more.studymanager.properties.GatewayProperties;
import io.redlink.more.studymanager.service.AuditService;
import io.redlink.more.studymanager.utils.MapperUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@EnableConfigurationProperties(GatewayProperties.class)
public class AuditLogAPIV1Controller implements AuditLogApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogAPIV1Controller.class);

    private final AuditService service;
    private final GatewayProperties properties;

    public AuditLogAPIV1Controller(AuditService service, GatewayProperties properties) {
        this.service = service;
        this.properties = properties;
    }

    @RequiresStudyRole({StudyRole.STUDY_ADMIN})
    public ResponseEntity<AuditLogMetadataDTO> getAuditLogMetadata(
            Long studyId) {
        long count = service.countAuditLogEntries(studyId);

        AuditLogMetadata metadata = new AuditLogMetadata(
                studyId,
                count
        );

        return ResponseEntity.ok(
                AuditlogTransformer.toAuditlogMetadataDTO_V1(metadata)
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN})
    public ResponseEntity<StreamingResponseBody> exportAuditLog(Long studyId) {
        List<AuditLog> dtos = service.listAuditLog(studyId);

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
                            prepareExportAuditLog(outputStream, studyId);
                        } catch (Exception e) {
                            LOGGER.warn("Error exporting study data for study_{}: {}", studyId, e.getMessage(), e);
                        }
                    });
        }
    }

    /**
     * Prepare the auditlog for streaming it to the export
     * @param outputStream outputStream for exported auditlog entries
     * @param studyId corresponding studyId of the auditlogs
     */
    private void prepareExportAuditLog(OutputStream outputStream, Long studyId) {
        Stream<AuditLog> auditlogEntries = service.getAuditLogs(
                studyId
        );

        try {
            var generator = MapperUtils.MAPPER.getFactory().createGenerator(outputStream, JsonEncoding.UTF8);
            generator.writeStartArray();
            // stream rausposten
            auditlogEntries.forEach(auditlogEntry -> {
                try {
                    MapperUtils.MAPPER.writeValue(generator, AuditlogTransformer.toAuditlogEntryDTO_V1(auditlogEntry));
                } catch(IOException e) {
                    throw new UncheckedIOException("Error exporting auditlogs for study " + studyId, e);
                }
            });
            generator.writeEndArray();
            generator.close();
        } catch(IOException e) {
            throw new UncheckedIOException("Error exporting auditlogs for study " + studyId, e);
        }
    }
}

