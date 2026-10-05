/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.model.survey;

/**
 * One answered cell of a question. A plain question has a single answer with a {@code null}
 * {@code subQuestionCode}; an array or multiple-choice question has one answer per subquestion.
 *
 * @param value the value as the participant submitted it, e.g. a free text or an answer option code
 * @param label the answer option's text for {@code value}, or {@code null} for free text and unknown codes
 */
public record Answer(
        String subQuestionCode,
        String subQuestionText,
        Object value,
        String label
) {}
