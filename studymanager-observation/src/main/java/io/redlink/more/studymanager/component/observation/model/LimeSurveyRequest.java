package io.redlink.more.studymanager.component.observation.model;

import java.util.List;

public record LimeSurveyRequest (
    String method,
    Object params,
    Integer id
){}
