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

public class SimpleParticipant {
    private final Integer id;
    private final Instant start;

    public SimpleParticipant(Integer id, Instant start) {
        this.id = id;
        this.start = start;
    }

    public Integer getId() {
        return id;
    }

    public Instant getStart() {
        return start;
    }
}
