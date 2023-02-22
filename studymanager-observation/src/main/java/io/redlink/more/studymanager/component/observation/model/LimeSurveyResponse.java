package io.redlink.more.studymanager.component.observation.model;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public record LimeSurveyResponse (
        List<Object> result,
        String error
){}
