package io.redlink.more.studymanager.model.data;

import com.fasterxml.jackson.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

public record ElasticObservationDataPoint(
        @JsonProperty("datapoint_id")
        String datapointId,
        @JsonProperty("participant_id")
        String participantId,
        @JsonProperty("study_id")
        String studyId,
        @JsonProperty("study_group_id")
        String studyGroupId,
        @JsonProperty("observation_id")
        String actionId,
        @JsonProperty("observation_type")
        String actionType,
        @JsonProperty("data_type")
        String dataType,
        @JsonProperty("storage_date")
        Instant storageDate,
        @JsonProperty("effective_time_frame")
        Instant effectiveTimeFrame,
        @JsonIgnore
        Map<String, Object> data
) implements ElasticDataPoint {

    private static final String DATA_FIELD_PREFIX = "data_";

    @JsonCreator
    public ElasticObservationDataPoint {
        data = Map.copyOf(data);
    }

    @JsonAnyGetter
    @JsonUnwrapped(prefix = "data_")
    Map<String, Object> dataMap() {
        // This is a dirty hack as @JsonUnwrapped does not work on Maps
        // (https://github.com/FasterXML/jackson-databind/issues/171)
        return data
                .entrySet()
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                        e -> DATA_FIELD_PREFIX + e.getKey(),
                        Map.Entry::getValue)
                );
    }
}
