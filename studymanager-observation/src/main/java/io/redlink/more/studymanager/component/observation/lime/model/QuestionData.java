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

/**
 * A LimeSurvey question. {@code code} is LimeSurvey's question {@code title}, which is also the key the
 * question's value is exported under.
 *
 * @param text HTML as authored in LimeSurvey; consumers are responsible for sanitizing it before display
 */
public record QuestionData(
        Integer questionId,
        Integer groupId,
        String code,
        String text,
        String type,
        Integer order,
        boolean mandatory,
        List<SubQuestionData> subQuestions,
        List<AnswerOptionData> answerOptions
) {
    public QuestionData {
        subQuestions = subQuestions == null ? List.of() : List.copyOf(subQuestions);
        answerOptions = answerOptions == null ? List.of() : List.copyOf(answerOptions);
    }
}
