/*
 * Copyright (c) 2022 Redlink GmbH.
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
