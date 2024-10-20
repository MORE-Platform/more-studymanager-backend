/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model.scheduler;

public class RelativeRecurrenceRule {

    private Duration frequency;

    private Duration endAfter;

    public RelativeRecurrenceRule() {
    }

    public Duration getFrequency() {
        return frequency;
    }

    public RelativeRecurrenceRule setFrequency(Duration frequency) {
        this.frequency = frequency;
        return this;
    }

    public Duration getEndAfter() {
        return endAfter;
    }

    public RelativeRecurrenceRule setEndAfter(Duration endAfter) {
        this.endAfter = endAfter;
        return this;
    }
}
