/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model.data;

import io.redlink.more.studymanager.api.v1.model.AuditlogMetadataDTO;

import java.util.Comparator;

public record AuditlogMetadata(
        Long studyId,
        int length,
        AuditlogMetadataDTO.FormatEnum format
) implements Comparable<AuditlogMetadata> {

    public static final Comparator<AuditlogMetadata> AUDITLOG_METADATA_COMPARATOR =
            Comparator.comparing(AuditlogMetadata::studyId);

    @Override
    public int compareTo(AuditlogMetadata compAuditlogMetadata) {
        return AUDITLOG_METADATA_COMPARATOR.compare(this, compAuditlogMetadata);
    }
}