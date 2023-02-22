package io.redlink.more.studymanager.component.observation.model;

import java.util.List;

public record LimeSurveyParticipantResponse (
    String error,
    List<ParticipantData> result,
    int id
) {}
