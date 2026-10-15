package io.redlink.more.studymanager.core.datavalidity;

import java.util.Map;

/**
 * @param resync the stored data is outdated and should be re-collected from its source with these resync
 *               properties, {@code null} if no resync is needed
 */
public record ObservationValidationResult(
        boolean invalid,
        ObservationDataState state,
        Map<String, Object> resync
) {
    public ObservationValidationResult(boolean invalid, ObservationDataState state) {
        this(invalid, state, null);
    }
}
