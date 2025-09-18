/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.core.ui;

public interface DataViewInfo {
    /**
     * Gets the name of the data view.
     * The name is the identifier of a specific view.
     *
     * @return the name of the data view
     */
    String name();

    /**
     * Gets the label of the data view.
     * The label is a short indicator to differ between multiple views.
     *
     * @return the label of the data view
     */
    String label();

    /**
     * Gets the title of the data view.
     * The title is a short textual information about the observation data.
     *
     * @return the title of the data view
     */
    String title();

    /**
     * Gets the description of the data view.
     * The description explains what the given observation data shows.
     *
     * @return the description of the data view
     */
    String description();

    /**
     * Gets the chart type of the data view.
     * The chartType indicates how the given observation data is visually shown.
     *
     * @return the chart type of the data view
     */
    DataView.ChartType chartType();
}