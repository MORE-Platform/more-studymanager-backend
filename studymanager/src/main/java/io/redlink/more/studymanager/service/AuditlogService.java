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
import io.redlink.more.studymanager.model.Auditlog;
import io.redlink.more.studymanager.repository.AuditlogRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class AuditlogService {
    private final AuditlogRepository auditlogRepository;

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

    public void deleteAuditlogById(long studyId, long auditlogId) {
        auditlogRepository.deleteAuditlogById(studyId, auditlogId);
    }

    public void deleteAuditlogsByStudyId(long studyId) {
        auditlogRepository.deleteAuditlogsByStudyId(studyId);
    }
}
