package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.EventDTO;
import io.redlink.more.studymanager.api.v1.model.ObservationDTO;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.utils.MapperUtils;

import java.time.OffsetDateTime;

public class ObservationTransformer {
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
                .setSchedule(dto.getSchedule());
    }

    public static ObservationDTO toObservationDTO_V1(Observation observation) {
        return new ObservationDTO()
                .studyId(observation.getStudyId())
                .observationId(observation.getObservationId())
                .title(observation.getTitle())
                .purpose(observation.getPurpose())
                .participantInfo(observation.getParticipantInfo())
                .type(observation.getType())
                .studyGroupId(observation.getStudyGroupId())
                .properties(observation.getProperties())
                .schedule(new EventDTO())
                .created(observation.getCreated().toLocalDateTime().atOffset(OffsetDateTime.now().getOffset()))
                .modified(observation.getModified().toLocalDateTime().atOffset(OffsetDateTime.now().getOffset()));
    }

}
