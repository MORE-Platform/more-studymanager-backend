package io.redlink.more.studymanager.component.observation.model;

import java.util.List;
import java.util.Map;

public record LimeSurveyRequest (
    String method,
    List<String> params,
    int id
){}
