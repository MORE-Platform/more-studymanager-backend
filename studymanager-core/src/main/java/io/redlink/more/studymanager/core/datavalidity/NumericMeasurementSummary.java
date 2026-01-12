package io.redlink.more.studymanager.core.datavalidity;

public record NumericMeasurementSummary(
        double min,
        double max,
        double avg,
        double sum,
        long missing
) {
}
