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
import io.redlink.more.studymanager.model.data.AuditLogMetadata;

import java.util.*;

public final class AuditlogTransformer {

    private AuditlogTransformer(){}

    public static AuditLogEntryDTO toAuditlogEntryDTO_V1(AuditLog auditLog) {
        Map<String,Object> details = new HashMap<>(auditLog.getDetails());
        //Both user_roles and study_roles are stored in the details, but exposed as special attribute in the DTO
        List<String> userRoles = Optional.ofNullable(
                details.remove("user_roles"))
                .filter(it -> it instanceof List)
                .map(List.class::cast)
                .map(it -> it.stream().map(Objects::toString).toList()
            ).orElse(null);
        List<String> studyRoles = Optional.ofNullable(
                        details.remove("study_roles"))
                .filter(it -> it instanceof List)
                .map(List.class::cast)
                .map(it -> it.stream().map(Objects::toString).toList()
            ).orElse(null);
        var auditLogEntry = new AuditLogEntryDTO()
                .id(auditLog.getId())
                .created(auditLog.getCreated())
                .studyId(auditLog.getStudyId())
                .userId(auditLog.getUserId())
                .userRoles(userRoles)
                .studyRoles(studyRoles)
                .userName(auditLog.getUserName())
                .timestamp(auditLog.getTimestamp())
                .action(auditLog.getAction())
                .actionState(AuditLogEntryDTO.ActionStateEnum.valueOf(auditLog.getActionState().name().toUpperCase()));

        details.forEach((s, o) -> auditLogEntry.putAdditionalProperty(s,o));

        return auditLogEntry;
    }

    public static AuditLogMetadataDTO toAuditlogMetadataDTO_V1(
            AuditLogMetadata auditLogMetadata) {
        return new AuditLogMetadataDTO()
                .studyId(auditLogMetadata.studyId())
                .length(auditLogMetadata.length());
    }
}
