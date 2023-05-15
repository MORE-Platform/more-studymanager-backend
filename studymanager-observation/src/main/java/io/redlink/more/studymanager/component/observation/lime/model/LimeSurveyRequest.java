package io.redlink.more.studymanager.component.observation.lime.model;

import java.util.List;

public record LimeSurveyRequest (
    String method,
    List<Object> params,
    Integer id
){}
