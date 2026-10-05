/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.component.observation.lime.model;

import java.util.List;

public record SurveyStructure(
        String surveyId,
        String language,
        List<QuestionGroupData> groups
) {
    public SurveyStructure {
        groups = groups == null ? List.of() : List.copyOf(groups);
    }

    public static SurveyStructure empty(String surveyId, String language) {
        return new SurveyStructure(surveyId, language, List.of());
    }
}
