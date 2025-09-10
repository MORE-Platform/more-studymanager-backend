/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.AuditlogDataDTO;
import io.redlink.more.studymanager.api.v1.model.AuditlogMetadataDTO;
import io.redlink.more.studymanager.api.v1.webservices.AuditlogApi;
import io.redlink.more.studymanager.controller.RequiresStudyRole;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.data.AuditlogMetadata;
import io.redlink.more.studymanager.model.transformer.AuditlogTransformer;
import io.redlink.more.studymanager.properties.GatewayProperties;
import io.redlink.more.studymanager.service.AuditlogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@EnableConfigurationProperties(GatewayProperties.class)
public class AuditlogAPIV1Controller implements AuditlogApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditlogAPIV1Controller.class);

    private final AuditlogService service;
    private final GatewayProperties properties;

    public AuditlogAPIV1Controller(AuditlogService service, GatewayProperties properties) {
        this.service = service;
        this.properties = properties;
    }

    @RequiresStudyRole({StudyRole.STUDY_ADMIN})
    public ResponseEntity<AuditlogMetadataDTO> getAuditlogMetadata(
            Long studyId,
            @RequestParam(required = false) String format) {
        int count = service.listAuditlogsByStudyId(studyId).size();

        AuditlogMetadataDTO.FormatEnum exportFormat;
        if (format == null || format.isBlank()) {
            exportFormat = AuditlogMetadataDTO.FormatEnum.JSON;
        } else {
            try {
                exportFormat = AuditlogMetadataDTO.FormatEnum.valueOf(format.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        AuditlogMetadata metadata = new AuditlogMetadata(
                studyId,
                count,
                exportFormat
        );

        return ResponseEntity.ok(
                AuditlogTransformer.toAuditlogMetadataDTO_V1(metadata)
        );
    }

    @RequiresStudyRole({StudyRole.STUDY_ADMIN})
    public ResponseEntity<List<AuditlogDataDTO>> listAuditlogs(Long studyId) {
        return ResponseEntity.ok(
                service.listAuditlogsByStudyId(studyId).stream()
                        .map(AuditlogTransformer::toAuditlogData)
                        .map(AuditlogTransformer::toAuditlogDataDTO_V1)
                        .toList()
        );
    }

    @RequiresStudyRole({StudyRole.STUDY_ADMIN})
    public ResponseEntity<AuditlogDataDTO> exportLastAuditlog(Long studyId) {
        return service.getLastAuditlog(studyId)
                .map(AuditlogTransformer::toAuditlogData)
                .map(AuditlogTransformer::toAuditlogDataDTO_V1)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN})
    public ResponseEntity<StreamingResponseBody> exportAuditlog(Long studyId) {
        List<AuditlogDataDTO> dtos = service.listAuditlogsByStudyId(studyId).stream()
                .map(AuditlogTransformer::toAuditlogData)
                .map(AuditlogTransformer::toAuditlogDataDTO_V1)
                .toList();

        if (dtos.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(dtos);
    }

    /* streaming result ---
    @RequiresStudyRole({StudyRole.STUDY_ADMIN})
    public ResponseEntity<StreamingResponseBody> exportAuditlog(Long studyId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=auditlogs_" + studyId + ".json");

        StreamingResponseBody stream = outputStream -> service.streamAuditlogs(outputStream, studyId);

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(stream);
    }*/
}
