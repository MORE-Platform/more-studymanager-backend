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

/**
 * Represents the configuration of a data view.
 *
 * @param filters the list of filters applied to the data view
 * @param rowAggregation the aggregation method for rows
 * @param seriesAggregation the aggregation method for series
 * @param operation the operation applied to the data
 */
public record ViewConfig(
        List<Filter> filters,
        Aggregation rowAggregation,
        Aggregation seriesAggregation,
        Operation operation
) {
    /**
     * Represents a filter applied to the data view.
     */
    public record Filter(

    ) {
    }

    /**
     * Enumeration of possible aggregation methods.
     */
    public enum Aggregation {
        TIME,
        STUDY_GROUP,
        PARTICIPANT,
        TERM_FIELD,
    }

    /**
     * Represents an operation applied to the data.
     *
     * @param operator the operator to be used
     * @param field the field to which the operation is applied
     */
    public record Operation(
            Operator operator,
            String field
    ) {
    }

    /**
     * Enumeration of possible operators.
     */
    public enum Operator {
        AVG,
        SUM,
        MIN,
        MAX,
        COUNT
    }
}
