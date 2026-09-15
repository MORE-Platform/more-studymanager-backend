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

/**
 * A pending request to re-collect the data of one observation for one participant. The request exists as
 * long as the row exists: the storage gateway picks it up and deletes it once the data has been synced.
 */
public record ObservationResyncRequest(
        Long studyId,
        Integer participantId,
        Integer observationId,
        Instant created
) {
}
