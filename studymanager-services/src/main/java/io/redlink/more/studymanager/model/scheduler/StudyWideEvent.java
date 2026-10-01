/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.model.scheduler;

/**
 * Schedule of a study-wide observation: it has nothing to configure and runs for the
 * complete study, from the effective start to the effective end.
 * <p>
 * It is never offered to clients as a schedule they can build. It exists so that the
 * stored schedule itself carries the signal, which is what lets services that only read
 * the database - the data-gateway above all, which has no access to the observation
 * factories - expand it to the full study range.
 */
public class StudyWideEvent implements ScheduleEvent {
    public static final String TYPE = "StudyWideEvent";
    // mutator for the type discriminator, which @JsonTypeInfo(visible = true) writes back
    private String type;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Randomization getRandomization() {
        return null;
    }

    @Override
    public ScheduleEvent setRandomization(Randomization randomization) {
        // a study-wide observation runs continuously, there is nothing to randomize
        return this;
    }
}
