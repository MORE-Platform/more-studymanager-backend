package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.webservices.ApplicationsApi;
import io.redlink.more.studymanager.controller.RequiresStudyRole;
import io.redlink.more.studymanager.model.LoginTokenApplication;
import io.redlink.more.studymanager.model.StudyRole;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class ApplicationsAPIV1Controller implements ApplicationsApi {
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR, StudyRole.STUDY_VIEWER})
    @Override
    public ResponseEntity<List<String>> getStudyApplications(Long studyId) {
        Set<String> availableApplications = Arrays.stream(LoginTokenApplication.values())
                .map(LoginTokenApplication::name)
                .collect(Collectors.toSet());
        return ResponseEntity.ok(new ArrayList<>(availableApplications));
    }
}
