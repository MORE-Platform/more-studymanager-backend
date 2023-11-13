/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.core.io;

public class RelativeTimeFrame implements TimeRange {

    private long timeInSec;

    public RelativeTimeFrame(long timeInSec) {
        this.timeInSec = timeInSec;
    }

    @Override
    public String getFromString() {
        return "now-" + timeInSec + "s";
    }

    @Override
    public String getToString() {
        return "now";
    }
}
