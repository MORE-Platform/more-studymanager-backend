package io.redlink.more.studymanager.component.observation.lime.model;

import java.util.List;

public record LimeSurveyParticipantResponse (
    String error,
    List<ParticipantData> result,
    int id
) {}
