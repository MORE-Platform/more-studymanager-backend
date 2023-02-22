package io.redlink.more.studymanager.component.observation.model;

import java.util.List;
import java.util.Map;

public record LimeSurveyListResponse(
        String error,
        List<Object> result,
        int id
) {
}
