package io.redlink.more.studymanager.core.datavalidity;

import java.util.List;

public record StringMeasurementSummary(
        List<FieldValue<String>> values
) {
}
