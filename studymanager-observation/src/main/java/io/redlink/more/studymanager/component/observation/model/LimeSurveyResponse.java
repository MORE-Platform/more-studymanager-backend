package io.redlink.more.studymanager.component.observation.model;

import java.util.HashMap;
import java.util.List;

public record LimeSurveyResponse (
  List<HashMap<String, String>> result,
  String error
){}
