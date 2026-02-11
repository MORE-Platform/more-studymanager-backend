/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.ObservationGroupDTO;
import io.redlink.more.studymanager.model.ObservationGroup;

import java.time.Instant;

public final class ObservationGroupTransformer {

    private ObservationGroupTransformer() {
    }

    public static ObservationGroup fromObservationGroupDTO_V1(ObservationGroupDTO ObservationGroupDTO) {
        return new ObservationGroup()
                .setStudyId(ObservationGroupDTO.getStudyId())
                .setObservationGroupId(ObservationGroupDTO.getObservationGroupId())
                .setTitle(ObservationGroupDTO.getTitle())
                .setPurpose(ObservationGroupDTO.getPurpose());
    }

    public static ObservationGroupDTO toObservationGroupDTO_V1(
            ObservationGroup ObservationGroup,
            int observationCount,
            int interventionCount,
            int participantCount
    ) {
        Instant instant = ObservationGroup.getModified();
        Instant instant1 = ObservationGroup.getCreated();
        return new ObservationGroupDTO()
                .studyId(ObservationGroup.getStudyId())
                .observationGroupId(ObservationGroup.getObservationGroupId())
                .title(ObservationGroup.getTitle())
                .purpose(ObservationGroup.getPurpose())
                .numberOfObservations(observationCount)
                .numberOfInterventions(interventionCount)
                .numberOfParticipants(participantCount)
                .created(instant1)
                .modified(instant);
    }



}
