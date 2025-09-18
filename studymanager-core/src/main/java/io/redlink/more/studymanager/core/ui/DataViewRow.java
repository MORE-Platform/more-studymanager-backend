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
 * Represents a row of data in a data view.
 *
 * @param label the label of the data row
 * @param values the list of values in the data row
 */
public record DataViewRow(
        String label,
        List<Double> values
) {
}
