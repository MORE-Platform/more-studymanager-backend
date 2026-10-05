/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.component.observation.lime.model;

/**
 * A subquestion of an array- or multiple-choice question. Its {@code code} appears in the exported response
 * as {@code <questionCode>[<subQuestionCode>]}.
 */
public record SubQuestionData(
        String code,
        String text,
        Integer order
) {}
