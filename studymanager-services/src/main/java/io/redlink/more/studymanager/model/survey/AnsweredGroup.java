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
 * A question group with its questions in display order.
 *
 * @param description HTML as authored in LimeSurvey; consumers are responsible for sanitizing it
 */
public record AnsweredGroup(
        Integer groupId,
        String title,
        String description,
        List<AnsweredQuestion> questions
) {
    public AnsweredGroup {
        questions = questions == null ? List.of() : List.copyOf(questions);
    }
}
