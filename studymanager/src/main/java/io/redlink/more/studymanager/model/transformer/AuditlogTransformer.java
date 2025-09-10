/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.*;
import io.redlink.more.studymanager.model.audit.AuditLog;
import io.redlink.more.studymanager.model.data.AuditlogMetadata;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class AuditlogTransformer {

    private AuditlogTransformer(){}

    public static AuditLogEntryDTO toAuditlogEntryDTO_V1(AuditLog auditLog) {
        return new AuditLogEntryDTO()
                .id(auditLog.getId())
                .studyId(auditLog.getStudyId())
                .userId(auditLog.getUserId())
                .userRoles(Optional.ofNullable(auditLog.getDetails().get("user_roles")).filter(it -> it instanceof List).map(List.class::cast).map(it -> it.stream().map(Objects::toString).toList()).orElse(null))
                //.userName(auditLog.getUserName()) ToDo getUserName From somewhere
                .timestamp(auditLog.getTimestamp())
                .action(auditLog.getAction())
                .actionState(AuditLogEntryDTO.ActionStateEnum.valueOf(auditLog.getActionState().name()))
                .details(auditLog.getDetails());
    }

    public static List<AuditLogEntryDTO> toAuditlogEntriesDTO_V1(List<AuditLog> auditLogEntries){
        if (auditLogEntries == null || auditLogEntries.isEmpty()) {
            return List.of();
        }
        return auditLogEntries.stream()
                .map(AuditlogTransformer::toAuditlogEntryDTO_V1)
                .toList();
    }

    public static AuditlogMetadataDTO toAuditlogMetadataDTO_V1(
            AuditlogMetadata auditlogMetadata) {
        return new AuditlogMetadataDTO()
                .studyId(auditlogMetadata.studyId())
                .length(auditlogMetadata.length())
                .format(auditlogMetadata.format());
    }
}
