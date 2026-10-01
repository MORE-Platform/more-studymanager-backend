/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.model.scheduler;

import io.redlink.more.studymanager.utils.MapperUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduleEventSerializationTest {

    @Test
    @DisplayName("StudyWideEvent round-trips through its type discriminator")
    void studyWideEventRoundTrips() {
        String json = MapperUtils.writeValueAsString(new StudyWideEvent());
        assertThat(json).contains("\"type\":\"StudyWideEvent\"");
        assertThat(MapperUtils.readValue(json, ScheduleEvent.class)).isInstanceOf(StudyWideEvent.class);
    }

    @Test
    @DisplayName("An unknown schedule type falls back to the default instead of failing")
    void unknownTypeFallsBackToDefaultImpl() {
        // services that do not know StudyWideEvent yet (an older data-gateway) must still be
        // able to read the observation; defaultImpl = Event.class gives them an empty schedule
        // rather than an InvalidTypeIdException, so there is no deployment ordering constraint
        assertThat(MapperUtils.readValue("{\"type\":\"SomeFutureEvent\"}", ScheduleEvent.class))
                .isInstanceOf(Event.class);
    }
}
