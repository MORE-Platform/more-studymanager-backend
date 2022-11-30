package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.StudyGroupDTO;
import io.redlink.more.studymanager.api.v1.webservices.StudyGroupsApi;
import io.redlink.more.studymanager.model.StudyGroup;
import io.redlink.more.studymanager.model.transformer.StudyGroupTransformer;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import io.redlink.more.studymanager.service.StudyGroupService;
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
public class StudyGroupApiV1Controller implements StudyGroupsApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(StudyGroupApiV1Controller.class);

    private final StudyGroupService service;

    private final OAuth2AuthenticationService authService;


    public StudyGroupApiV1Controller(StudyGroupService service, OAuth2AuthenticationService authService) {
        this.service = service;
        this.authService = authService;
    }

    @Override
    public ResponseEntity<StudyGroupDTO> createStudyGroup(Long studyId, StudyGroupDTO studyGroupDTO) {
        final var currentUser = authService.getCurrentUser();
        StudyGroup studyGroup = service.createStudyGroup(
                StudyGroupTransformer.fromStudyGroupDTO_V1(studyGroupDTO), currentUser
        );
        LOGGER.debug("StudyGroup created: {}", studyGroup);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                StudyGroupTransformer.toStudyGroupDTO_V1(studyGroup)
        );
    }

    @Override
    public ResponseEntity<List<StudyGroupDTO>> listStudyGroups(Long studyId) {
        final var currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(
                service.listStudyGroups(studyId, currentUser).stream()
                        .map(StudyGroupTransformer::toStudyGroupDTO_V1)
                        .toList()
        );
    }

    @Override
    public ResponseEntity<StudyGroupDTO> getStudyGroup(Long studyId, Integer studyGroupId) {
        final var currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(
                StudyGroupTransformer.toStudyGroupDTO_V1(service.getStudyGroup(studyId, studyGroupId, currentUser))
        );
    }

    @Override
    public ResponseEntity<StudyGroupDTO> updateStudyGroup(Long studyId, Integer studyGroupId, StudyGroupDTO studyGroupDTO) {
        final var currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(
                StudyGroupTransformer.toStudyGroupDTO_V1(
                        service.updateStudyGroup(
                                StudyGroupTransformer.fromStudyGroupDTO_V1(studyGroupDTO),
                                currentUser
                        )
                )
        );
    }

    @Override
    public ResponseEntity<Void> deleteStudyGroup(Long studyId, Integer studyGroupId) {
        final var currentUser = authService.getCurrentUser();
        service.deleteStudyGroup(studyId, studyGroupId, currentUser);
        return ResponseEntity.noContent().build();
    }
}
