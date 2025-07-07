/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.core.ui;

/**
 * Represents a data view with information, chart type, and data.
 *
 * @param viewInfo the information about the data view
 * @param chartType the type of chart to be displayed
 * @param data the data to be displayed in the view
 */
public record DataView(
        DataViewInfo viewInfo,
        ChartType chartType,
        DataViewData data
) {
    /**
     * Enumeration of possible chart types.
     */
    public enum ChartType {
        LINE,
        BAR,
        PIE
    }
}

