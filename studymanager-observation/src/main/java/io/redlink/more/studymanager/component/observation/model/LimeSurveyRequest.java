package io.redlink.more.studymanager.component.observation.model;

import java.util.List;
import java.util.Map;

public record LimeSurveyRequest (
    String method,
    Map<Object, Object> params,
    int id
){}
