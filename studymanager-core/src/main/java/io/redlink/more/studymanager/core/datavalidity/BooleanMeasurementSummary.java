package io.redlink.more.studymanager.core.datavalidity;

import java.util.List;

public record BooleanMeasurementSummary(
        List<BooleanFieldValue> values
) {
}
