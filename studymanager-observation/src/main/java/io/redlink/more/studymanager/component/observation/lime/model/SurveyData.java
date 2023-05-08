package io.redlink.more.studymanager.component.observation.lime.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SurveyData(
        @JsonProperty("sid")
        String surveyId,
        @JsonProperty("surveyls_title")
        String surveyTitle
) {
}
