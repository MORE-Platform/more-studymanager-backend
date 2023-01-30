package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.StatusChangeDTO;
import io.redlink.more.studymanager.api.v1.model.StudyDTO;
import io.redlink.more.studymanager.api.v1.webservices.StudiesApi;
import io.redlink.more.studymanager.controller.RequiresStudyRole;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.transformer.StudyTransformer;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import io.redlink.more.studymanager.service.StudyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class StudyApiV1Controller implements StudiesApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(StudyApiV1Controller.class);

    private final StudyService service;

    private final OAuth2AuthenticationService authService;

    public StudyApiV1Controller(StudyService service, OAuth2AuthenticationService authService) {
        this.service = service;
        this.authService = authService;
    }

    @Override
    public ResponseEntity<StudyDTO> createStudy(StudyDTO studyDTO) {
        final var currentUser = authService.getCurrentUser();
        Study study = service.createStudy(StudyTransformer.fromStudyDTO_V1(studyDTO), currentUser);
        LOGGER.debug("{} created a study {}", currentUser, study);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                StudyTransformer.toStudyDTO_V1(study)
        );
    }

    @Override
    public ResponseEntity<List<StudyDTO>> listStudies() {
        final var currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(
                service.listStudies(currentUser).stream()
                        .map(StudyTransformer::toStudyDTO_V1)
                        .toList()
        );
    }

    @Override
    @RequiresStudyRole
    public ResponseEntity<StudyDTO> getStudy(Long studyId) {
        final var currentUser = authService.getCurrentUser();
        return ResponseEntity.of(
                service.getStudy(studyId, currentUser)
                        .map(StudyTransformer::toStudyDTO_V1)
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<StudyDTO> updateStudy(Long studyId, StudyDTO studyDTO) {
        final var currentUser = authService.getCurrentUser();
        return ResponseEntity.of(
                service.updateStudy(
                        StudyTransformer.fromStudyDTO_V1(studyDTO), currentUser
                ).map(StudyTransformer::toStudyDTO_V1)
        );
    }

    @Override
    @RequiresStudyRole(StudyRole.STUDY_ADMIN)
    public ResponseEntity<Void> deleteStudy(Long studyId) {
        service.deleteStudy(studyId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @RequiresStudyRole(StudyRole.STUDY_ADMIN)
    public ResponseEntity<Void> setStatus(Long studyId, StatusChangeDTO statusChangeDTO) {
        final var currentUser = authService.getCurrentUser();
        service.setStatus(studyId, StudyTransformer.fromStatusChangeDTO_V1(statusChangeDTO), currentUser);
        return ResponseEntity.ok().build();
    }
}
