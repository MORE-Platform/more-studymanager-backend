/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.model.survey;

import java.util.List;

/**
 * Everything needed to display a participant's LimeSurvey submissions: the survey's groups and questions,
 * with the participant's answers resolved against them, once per selected response.
 */
public record ParticipantSurveyResponses(
        Long studyId,
        Integer observationId,
        Integer participantId,
        String surveyId,
        String language,
        List<SurveyResponse> responses
) {
    public ParticipantSurveyResponses {
        responses = responses == null ? List.of() : List.copyOf(responses);
    }
}
