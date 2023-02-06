package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.ParticipationDataDTO;
import io.redlink.more.studymanager.model.ParticipationData;

public class StudyDataTransformer {

    private StudyDataTransformer(){}

    public static ParticipationDataDTO toParticipationDataDTO_V1(ParticipationData participationData){
        return new ParticipationDataDTO()
                .observationId(participationData.getObservationId())
                .participantId(participationData.getParticipantId())
                .studyGroupId(participationData.getStudyGroupId())
                .dataReceived(participationData.isDataReceived())
                .lastDataReceived(participationData.getLastDataReceived());
    }
}
