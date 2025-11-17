/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model.data;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

public record ElasticActionDataPoint (
        @JsonProperty("datapoint_id")
        String datapointId,
        @JsonProperty("participant_id")
        String participantId,
        @JsonProperty("study_id")
        String studyId,
        @JsonProperty("study_group_id")
        String studyGroupId,
        @JsonProperty("action_id")
        String actionId,
        @JsonProperty("action_type")
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
    public ElasticActionDataPoint {
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
