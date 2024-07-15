/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.core.ui;

import java.util.List;

public record ViewConfig(
        List<Filter> filters,
        Aggregation rowAggregation,
        Aggregation seriesAggregation,
        Operation operation
) {

    public record Filter(

    ) {
    }

    public enum Aggregation {
        TIME,
        STUDY_GROUP,
        PARTICIPANT,
        TERM_FIELD,
    }

    public record Operation(
            Operator operator,
            String field
    ) {
    }

    public enum Operator {
        AVG,
        SUM,
        MIN,
        MAX,
        COUNT
    }
}
