/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model;

import java.time.Instant;

public record MoreUser(
        String id,
        String fullName,
        String institution,
        String email,
        Instant inserted,
        Instant updated
) implements User {

    public MoreUser(String id, String fullName, String institution, String email) {
        this(id, fullName, institution, email, null, null);
    }
}
