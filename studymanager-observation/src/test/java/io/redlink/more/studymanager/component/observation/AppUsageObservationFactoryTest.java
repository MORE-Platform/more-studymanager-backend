/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.component.observation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AppUsageObservationFactoryTest {

    @Test
    void testIsStudyWide() {
        Assertions.assertTrue(new AppUsageObservationFactory<>().isStudyWide(),
                "app usage is collected continuously, so it must run for the whole study");
        Assertions.assertFalse(new QuestionObservationFactory().isStudyWide(),
                "study-wide is opt-in, other observations must default to false");
    }
}
