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
import io.redlink.more.studymanager.model.Auditlog;
import io.redlink.more.studymanager.model.data.AuditlogData;
import io.redlink.more.studymanager.model.data.AuditlogMetadata;

import java.util.List;

public final class AuditlogTransformer {

    private AuditlogTransformer(){}

    public static AuditlogData toAuditlogData(Auditlog auditlog) {
        return new AuditlogData(
                auditlog.getStudyId(),
                auditlog.getAuditlogId(),
                auditlog.getUserId(),
                auditlog.getUserRoles(),
                auditlog.getUserName(),
                auditlog.getTimestamp(),
                auditlog.getAction(),
                auditlog.getState(),
                auditlog.getDetails()
        );
    }

    public static AuditlogDataDTO toAuditlogDataDTO_V1(AuditlogData auditlogData) {
        return new AuditlogDataDTO()
                .studyId(auditlogData.studyId())
                .auditlogId(auditlogData.auditlogId())
                .userId(auditlogData.userId())
                .userRoles(auditlogData.userRoles())
                .userName(auditlogData.userName())
                .timestamp(auditlogData.timestamp())
                .action(auditlogData.action())
                .state(auditlogData.state())
                .details(auditlogData.details());
    }
    public static List<AuditlogDataDTO> toAuditlogDataDTO_V1(List<AuditlogData> auditlogData){
        if (auditlogData == null || auditlogData.isEmpty()) {
            return List.of();
        }
        return auditlogData.stream()
                .map(AuditlogTransformer::toAuditlogDataDTO_V1)
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
