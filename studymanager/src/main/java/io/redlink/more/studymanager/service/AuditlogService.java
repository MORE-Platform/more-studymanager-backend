/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.more.studymanager.api.v1.model.AuditlogDataDTO;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.model.Auditlog;
import io.redlink.more.studymanager.model.transformer.AuditlogTransformer;
import io.redlink.more.studymanager.repository.AuditlogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class AuditlogService {
    private final AuditlogRepository auditlogRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditlogService.class);


    public AuditlogService(
            AuditlogRepository auditlogRepository) {
        this.auditlogRepository = auditlogRepository;
    }

    public Auditlog insertAuditlogData(Auditlog auditlog) {
        return auditlogRepository.insertAuditlog(auditlog);
    }

    public Optional<Auditlog> getAuditlogById(long studyId, long auditlogId) {
        try {
            return Optional.ofNullable(auditlogRepository.getAuditlogById(studyId, auditlogId));
        } catch (BadRequestException e) {
            return Optional.empty();
        }
    }

    public List<Auditlog> listAuditlogsByStudyId(long studyId) {
            List<Auditlog> result = auditlogRepository.listAuditlogsByStudyId(studyId);
            return result != null ? result : Collections.emptyList();
    }

    public Optional<Auditlog> getLastAuditlog(Long studyId) {
        List<Auditlog> auditlogs = listAuditlogsByStudyId(studyId);
        if (auditlogs.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(auditlogs.get(auditlogs.size() - 1));
    }

    public void deleteAuditlogById(long studyId, long auditlogId) {
        auditlogRepository.deleteAuditlogById(studyId, auditlogId);
    }

    public void deleteAuditlogsByStudyId(long studyId) {
        auditlogRepository.deleteAuditlogsByStudyId(studyId);
    }

    // streaming helper function
    public void exportAuditlogs(OutputStream outputStream, Long studyId) {
        List<Auditlog> auditlogs = auditlogRepository.listAuditlogsByStudyId(studyId);

        ObjectMapper mapper = new ObjectMapper();
        try (JsonGenerator generator = mapper.getFactory().createGenerator(outputStream)) {
            generator.writeStartArray();
            for (Auditlog auditlog : auditlogs) {
                AuditlogDataDTO dto = AuditlogTransformer.toAuditlogDataDTO_V1(
                        AuditlogTransformer.toAuditlogData(auditlog));
                mapper.writeValue(generator, dto);
            }
            generator.writeEndArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Error exporting auditlogs for study " + studyId, e);
        }
    }

}
