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
 * A single selectable answer of a LimeSurvey question. {@code code} is the value that ends up in the
 * exported response, {@code label} is the text shown to the participant.
 */
public record AnswerOptionData(
        String code,
        String label,
        Integer order
) {}
