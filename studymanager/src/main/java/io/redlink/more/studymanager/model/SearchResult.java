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
