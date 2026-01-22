package io.redlink.more.studymanager.core.datavalidity;

public record ObservationValidationResult(
        boolean invalid,
        ObservationDataState state
) {
}
