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
            ObservationGroup observationGroup,
            int observationCount,
            int interventionCount,
            int participantCount
    ) {
        return toObservationGroupDTO_V1(observationGroup)
                .numberOfObservations(observationCount)
                .numberOfInterventions(interventionCount)
                .numberOfParticipants(participantCount);
    }

    public static ObservationGroupDTO toObservationGroupDTO_V1(
            ObservationGroup observationGroup
    ) {
        return new ObservationGroupDTO()
                .studyId(observationGroup.getStudyId())
                .observationGroupId(observationGroup.getObservationGroupId())
                .title(observationGroup.getTitle())
                .purpose(observationGroup.getPurpose())
                .created(observationGroup.getCreated())
                .modified(observationGroup.getModified());
    }



}
