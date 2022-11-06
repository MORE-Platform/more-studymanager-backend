package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.StudyGroupDTO;
import io.redlink.more.studymanager.api.v1.webservices.StudyGroupsApi;
import io.redlink.more.studymanager.model.StudyGroup;
import io.redlink.more.studymanager.model.transformer.StudyGroupTransformer;
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

    public StudyGroupApiV1Controller(StudyGroupService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<StudyGroupDTO> createStudyGroup(Long studyId, StudyGroupDTO studyGroupDTO) {
        StudyGroup studyGroup = service.createStudyGroup(StudyGroupTransformer.fromStudyGroupDTO_V1(studyGroupDTO));
        LOGGER.debug("StudyGroup created: {}", studyGroup);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                StudyGroupTransformer.toStudyGroupDTO_V1(studyGroup)
        );
    }

    @Override
    public ResponseEntity<List<StudyGroupDTO>> listStudyGroups(Long studyId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                service.listStudyGroups(studyId).stream().map(StudyGroupTransformer::toStudyGroupDTO_V1).toList()
        );
    }

    @Override
    public ResponseEntity<StudyGroupDTO> getStudyGroup(Long studyId, Long studyGroupId) {
        return ResponseEntity.ok(
                StudyGroupTransformer.toStudyGroupDTO_V1(service.getStudyGroup(studyId, studyGroupId.intValue()))
        );
    }

    @Override
    public ResponseEntity<StudyGroupDTO> updateStudyGroup(Long studyId, Long studyGroupId, StudyGroupDTO studyGroupDTO) {
        return ResponseEntity.ok(
                StudyGroupTransformer.toStudyGroupDTO_V1(
                        service.updateStudyGroup(
                                StudyGroupTransformer.fromStudyGroupDTO_V1(studyGroupDTO)
                        )
                )
        );
    }

    @Override
    public ResponseEntity<Void> deleteStudyGroup(Long studyId, Long studyGroupId) {
        service.deleteStudyGroup(studyId, studyGroupId.intValue());
        return ResponseEntity.noContent().build();
    }
}
