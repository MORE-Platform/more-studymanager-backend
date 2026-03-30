package io.redlink.more.studymanager.component.observation.lime.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ParticipantCreationData(
        String firstname,
        String lastname,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String token
) {
}
