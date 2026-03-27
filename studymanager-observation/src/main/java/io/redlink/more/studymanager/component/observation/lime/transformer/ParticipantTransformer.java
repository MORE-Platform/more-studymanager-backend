package io.redlink.more.studymanager.component.observation.lime.transformer;

import io.redlink.more.studymanager.component.observation.lime.model.ParticipantCreationData;
import io.redlink.more.studymanager.component.observation.lime.model.ParticipantData;

public class ParticipantTransformer {
    public static ParticipantData transformToData(ParticipantCreationData requestData) {
        return new ParticipantData(
                new ParticipantData.ParticipantInfo(requestData.firstname(), requestData.lastname()),
                requestData.token(),
                null
        );
    }
}
