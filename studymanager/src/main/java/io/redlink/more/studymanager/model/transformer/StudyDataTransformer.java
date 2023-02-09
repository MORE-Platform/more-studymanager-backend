package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.ParticipationDataDTO;
import io.redlink.more.studymanager.model.ParticipationData;

public class StudyDataTransformer {

    private StudyDataTransformer(){}

    public static ParticipationDataDTO toParticipationDataDTO_V1(ParticipationData participationData){
        return new ParticipationDataDTO()
                .observationId(participationData.observationId())
                .participantId(participationData.participantId())
                .studyGroupId(participationData.studyGroupId())
                .dataReceived(participationData.dataReceived())
                .lastDataReceived(Transformers.toOffsetDateTime(participationData.lastDataReceived()));
    }
}
