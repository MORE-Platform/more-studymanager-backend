package io.redlink.more.studymanager.core.datavalidity;

import java.time.Instant;

public record DateMeasurementSummary(
        Instant min,
        Instant max,
        long missing
) {
}
