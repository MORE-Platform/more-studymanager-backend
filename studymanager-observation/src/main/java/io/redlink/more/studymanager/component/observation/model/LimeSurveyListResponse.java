package io.redlink.more.studymanager.component.observation.model;

import java.util.List;

public record LimeSurveyListResponse(
        String error,
        List<Object> result,
        int id
) {}
