/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model.data;

import io.redlink.more.studymanager.api.v1.model.ActionDTO;
import io.redlink.more.studymanager.api.v1.model.AuditlogDataDTO;
import io.redlink.more.studymanager.api.v1.model.StudyRoleDTO;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record AuditlogData(
        Long studyId,
        UUID auditlogId,
        String userId,
        List<StudyRoleDTO> userRoles,
        String userName,
        Instant timestamp,
        ActionDTO action,
        AuditlogDataDTO.StateEnum state,
        Map<String, Object> details
) implements Comparable<AuditlogData> {

    public static final Comparator<AuditlogData> AUDITLOG_DATA_COMPARATOR =
            Comparator.comparing(AuditlogData::auditlogId)
                    .thenComparing(AuditlogData::timestamp)
                    .thenComparing(AuditlogData::state);

    @Override
    public int compareTo(AuditlogData compAuditLogData) {
        return AUDITLOG_DATA_COMPARATOR.compare(this, compAuditLogData);
    }
}