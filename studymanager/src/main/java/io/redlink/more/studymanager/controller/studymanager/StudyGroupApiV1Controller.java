/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.StudyGroupDTO;
import io.redlink.more.studymanager.api.v1.webservices.StudyGroupsApi;
import io.redlink.more.studymanager.controller.RequiresStudyRole;
import io.redlink.more.studymanager.model.StudyGroup;
import io.redlink.more.studymanager.model.StudyRole;
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
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<StudyGroupDTO> createStudyGroup(Long studyId, StudyGroupDTO studyGroupDTO) {
        StudyGroup studyGroup = service.createStudyGroup(
                StudyGroupTransformer.fromStudyGroupDTO_V1(studyGroupDTO)
        );
        LOGGER.debug("StudyGroup created: {}", studyGroup);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                StudyGroupTransformer.toStudyGroupDTO_V1(studyGroup)
        );
    }

    @Override
    @RequiresStudyRole
    public ResponseEntity<List<StudyGroupDTO>> listStudyGroups(Long studyId) {
        return ResponseEntity.ok(
                service.listStudyGroups(studyId).stream()
                        .map(StudyGroupTransformer::toStudyGroupDTO_V1)
                        .toList()
        );
    }

    @Override
    @RequiresStudyRole
    public ResponseEntity<StudyGroupDTO> getStudyGroup(Long studyId, Integer studyGroupId) {
        return ResponseEntity.ok(
                StudyGroupTransformer.toStudyGroupDTO_V1(service.getStudyGroup(studyId, studyGroupId))
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<StudyGroupDTO> updateStudyGroup(Long studyId, Integer studyGroupId, StudyGroupDTO studyGroupDTO) {
        return ResponseEntity.ok(
                StudyGroupTransformer.toStudyGroupDTO_V1(
                        service.updateStudyGroup(
                                StudyGroupTransformer.fromStudyGroupDTO_V1(studyGroupDTO)
                        )
                )
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<Void> deleteStudyGroup(Long studyId, Integer studyGroupId) {
        service.deleteStudyGroup(studyId, studyGroupId);
        return ResponseEntity.noContent().build();
    }
}
