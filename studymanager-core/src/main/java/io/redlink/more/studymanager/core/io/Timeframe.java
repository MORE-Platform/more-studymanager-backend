/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.core.io;

import java.time.Instant;

public class Timeframe implements TimeRange {

    private Instant from;
    private Instant to;

    public Timeframe(Instant from, Instant to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public String getFromString() {
        return from != null ? from.toString() : null;
    }

    @Override
    public String getToString() {
        return to != null ? to.toString() : null;
    }
}
