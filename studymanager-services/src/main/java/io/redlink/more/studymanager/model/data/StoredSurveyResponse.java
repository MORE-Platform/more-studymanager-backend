/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.model.data;

import java.time.Instant;
import java.util.Map;

/**
 * One LimeSurvey response as it is stored in Elasticsearch. {@code values} holds the indexed {@code data_*}
 * fields with their prefix stripped, so its keys are the LimeSurvey question codes - including the array form
 * {@code <questionCode>[<subQuestionCode>]} - plus LimeSurvey's own metadata keys such as {@code id},
 * {@code seed} and {@code lastpage}.
 *
 * @param responseId the LimeSurvey response id ({@code data_id}), the value the survey end url passes as savedid
 */
public record StoredSurveyResponse(
        String datapointId,
        Integer responseId,
        String seed,
        Instant effectiveTimeFrame,
        Instant storageDate,
        Map<String, Object> values
) {
    public StoredSurveyResponse {
        values = values == null ? Map.of() : Map.copyOf(values);
    }
}
