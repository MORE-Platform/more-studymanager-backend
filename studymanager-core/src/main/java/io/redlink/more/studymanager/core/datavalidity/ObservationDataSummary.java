package io.redlink.more.studymanager.core.datavalidity;

import java.util.List;

public record ObservationDataSummary(
    long numDocs,
    DateMeasurementSummary effectiveTime,
    List<MeasurementSummary> measurements
) {}