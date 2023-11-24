/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.InterventionDTO;
import io.redlink.more.studymanager.model.Intervention;

public final class InterventionTransformer {

    private InterventionTransformer() {
    }

    public static Intervention fromInterventionDTO_V1(InterventionDTO dto) {
        return new Intervention()
                .setStudyId(dto.getStudyId())
                .setInterventionId(dto.getInterventionId())
                .setTitle(dto.getTitle())
                .setPurpose(dto.getPurpose())
                .setStudyGroupId(dto.getStudyGroupId())
                .setSchedule(EventTransformer.fromObservationScheduleDTO_V1(dto.getSchedule()));
    }

    public static InterventionDTO toInterventionDTO_V1(Intervention intervention) {
        return new InterventionDTO()
                .studyId(intervention.getStudyId())
                .interventionId(intervention.getInterventionId())
                .title(intervention.getTitle())
                .purpose(intervention.getPurpose())
                .studyGroupId(intervention.getStudyGroupId())
                .schedule(EventTransformer.toObservationScheduleDTO_V1(intervention.getSchedule()))
                .created(Transformers.toOffsetDateTime(intervention.getCreated()))
                .modified(Transformers.toOffsetDateTime(intervention.getModified()));
    }

}
