/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model;

import org.apache.commons.lang3.StringUtils;

public interface User {
    String id();

    String fullName();

    String email();

    String institution();

    default boolean isValid() {
        return StringUtils.isNoneBlank(
                id(),
                fullName(),
                email()
                );
    }
}
