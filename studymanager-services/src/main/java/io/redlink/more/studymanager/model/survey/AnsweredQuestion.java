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
 * A question together with the participant's answers to it. {@code answers} is empty when the question was
 * not answered, so a consumer can render unanswered questions too.
 *
 * @param text HTML as authored in LimeSurvey; consumers are responsible for sanitizing it before display
 */
public record AnsweredQuestion(
        Integer questionId,
        String code,
        String text,
        String type,
        boolean mandatory,
        List<Answer> answers
) {
    public AnsweredQuestion {
        answers = answers == null ? List.of() : List.copyOf(answers);
    }

    public boolean isAnswered() {
        return !answers.isEmpty();
    }
}
