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
 * Represents the data in a data view.
 *
 * @param labels the labels for the data
 * @param rows the rows of data
 */
public record DataViewData(
        List<String> labels,
        List<DataViewRow> rows
) {
}
