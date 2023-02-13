package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.IdTitleDTO;
import io.redlink.more.studymanager.api.v1.model.ParticipationDataDTO;
import io.redlink.more.studymanager.model.ParticipationData;

public class StudyDataTransformer {

    private StudyDataTransformer(){}

    public static ParticipationDataDTO toParticipationDataDTO_V1(ParticipationData participationData){
        return new ParticipationDataDTO()
                .observationNamedId(toIdTitleDTO_V1(participationData.observationNamedId()))
                .observationType(participationData.observationType())
                .participantNamedId(toIdTitleDTO_V1(participationData.participantNamedId()))
                .studyGroupNamedId(toIdTitleDTO_V1(participationData.studyGroupNamedId()))
                .dataReceived(participationData.dataReceived())
                .lastDataReceived(Transformers.toOffsetDateTime(participationData.lastDataReceived()));
    }
    public static IdTitleDTO toIdTitleDTO_V1(ParticipationData.NamedId idTitle){
        if(idTitle == null)
            return null;
        return new IdTitleDTO()
                .id(idTitle.id())
                .title(idTitle.title());
    }
}
