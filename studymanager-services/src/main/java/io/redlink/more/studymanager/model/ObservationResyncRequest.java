/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.model;

import java.time.Instant;
import java.util.Map;

/**
 * A pending request to re-collect the data of one observation for one participant. The request exists as
 * long as the row exists: the storage gateway picks it up and deletes it once the data has been synced.
 *
 * @param properties details for the storage gateway, e.g. why the data health check requested the resync. Empty by default
 */
public record ObservationResyncRequest(
        Long studyId,
        Integer participantId,
        Integer observationId,
        Instant created,
        Map<String, Object> properties
) {
    public ObservationResyncRequest(Long studyId, Integer participantId, Integer observationId, Instant created) {
        this(studyId, participantId, observationId, created, Map.of());
    }
}
