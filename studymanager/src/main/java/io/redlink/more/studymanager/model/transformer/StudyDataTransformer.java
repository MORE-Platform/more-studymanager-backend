package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.IdTitleDTO;
import io.redlink.more.studymanager.api.v1.model.ParticipationDataDTO;
import io.redlink.more.studymanager.model.ParticipationData;

public class StudyDataTransformer {

    private StudyDataTransformer(){}

    public static ParticipationDataDTO toParticipationDataDTO_V1(ParticipationData participationData){
        return new ParticipationDataDTO()
                .observationData(toIdTitleDTO_V1(participationData.observationData()))
                .participantData(toIdTitleDTO_V1(participationData.participantData()))
                .studyGroupData(toIdTitleDTO_V1(participationData.studyGroupData()))
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
