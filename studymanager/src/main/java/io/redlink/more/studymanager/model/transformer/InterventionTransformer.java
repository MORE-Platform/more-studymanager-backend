package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.InterventionDTO;
import io.redlink.more.studymanager.model.Intervention;
import java.time.ZoneOffset;

public class InterventionTransformer {

    public static Intervention fromInterventionDTO_V1(InterventionDTO dto) {
        return new Intervention()
                .setStudyId(dto.getStudyId())
                .setInterventionId(dto.getInterventionId())
                .setTitle(dto.getTitle())
                .setPurpose(dto.getPurpose())
                .setStudyGroupId(dto.getStudyGroupId())
                .setSchedule(dto.getSchedule());
    }

    public static InterventionDTO toInterventionDTO_V1(Intervention intervention) {
        return new InterventionDTO()
                .studyId(intervention.getStudyId())
                .interventionId(intervention.getInterventionId())
                .title(intervention.getTitle())
                .purpose(intervention.getPurpose())
                .studyGroupId(intervention.getStudyGroupId())
                .schedule(intervention.getSchedule())
                .created(intervention.getCreated().atOffset(ZoneOffset.UTC))
                .modified(intervention.getModified().atOffset(ZoneOffset.UTC));
    }

}
