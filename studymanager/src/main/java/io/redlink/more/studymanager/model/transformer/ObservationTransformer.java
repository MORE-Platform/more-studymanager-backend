/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.ObservationDTO;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.utils.MapperUtils;
import java.time.Instant;

public final class ObservationTransformer {

    private ObservationTransformer() {
    }

    public static Observation fromObservationDTO_V1(ObservationDTO dto) {
        return new Observation()
                .setStudyId(dto.getStudyId())
                .setObservationId(dto.getObservationId())
                .setTitle(dto.getTitle())
                .setPurpose(dto.getPurpose())
                .setParticipantInfo(dto.getParticipantInfo())
                .setType(dto.getType())
                .setStudyGroupId(dto.getStudyGroupId())
                .setProperties(MapperUtils.MAPPER.convertValue(dto.getProperties(), ObservationProperties.class))
                .setSchedule(EventTransformer.fromObservationScheduleDTO_V1(dto.getSchedule()))
                .setHidden(dto.getHidden())
                .setNoSchedule(dto.getNoSchedule());
    }

    public static ObservationDTO toObservationDTO_V1(Observation observation) {
        Instant instant = observation.getModified();
        Instant instant1 = observation.getCreated();
        return new ObservationDTO()
                .studyId(observation.getStudyId())
                .observationId(observation.getObservationId())
                .title(observation.getTitle())
                .purpose(observation.getPurpose())
                .participantInfo(observation.getParticipantInfo())
                .type(observation.getType())
                .studyGroupId(observation.getStudyGroupId())
                .properties(observation.getProperties())
                .schedule(EventTransformer.toObservationScheduleDTO_V1(observation.getSchedule()))
                .created(instant1)
                .modified(instant)
                .hidden(observation.getHidden())
                .noSchedule(observation.getNoSchedule());
    }

}
