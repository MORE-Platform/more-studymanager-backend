package io.redlink.more.studymanager.component.observation.lime.model;

import java.util.List;

public record LimeSurveyParticipantCreationResponse(
        String error,
        List<ParticipantCreationData> result,
        int id
) {

}