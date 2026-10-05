/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.model.survey;

import java.time.Instant;
import java.util.List;

/**
 * A single submission of a survey by a participant, with every group and question of the survey resolved
 * against the values of that submission.
 *
 * @param responseId the LimeSurvey response id, i.e. the savedid of this submission
 * @param source     where the values came from
 */
public record SurveyResponse(
        Integer responseId,
        String seed,
        Instant submitted,
        Source source,
        List<AnsweredGroup> groups
) {
    public enum Source {
        STORED,
        LIME
    }

    public SurveyResponse {
        groups = groups == null ? List.of() : List.copyOf(groups);
    }
}
