/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model;

import java.util.List;

public record SearchResult<T>(
        long numFound,
        int offset,
        List<T> content
) {

    public SearchResult {
        content = List.copyOf(content);
    }

    public SearchResult() {
        this(0, 0, List.of());
    }
}
