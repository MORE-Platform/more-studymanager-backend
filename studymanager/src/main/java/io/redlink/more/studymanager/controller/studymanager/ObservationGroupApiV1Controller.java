/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.ObservationGroupDTO;
import io.redlink.more.studymanager.api.v1.webservices.ObservationGroupsApi;
import io.redlink.more.studymanager.audit.Audited;
import io.redlink.more.studymanager.controller.RequiresStudyRole;
import io.redlink.more.studymanager.model.ObservationGroup;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.transformer.ObservationGroupTransformer;
import io.redlink.more.studymanager.service.ObservationGroupService;
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
public class ObservationGroupApiV1Controller implements ObservationGroupsApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObservationGroupApiV1Controller.class);

    private final ObservationGroupService service;


    public ObservationGroupApiV1Controller(ObservationGroupService service) {
        this.service = service;
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    @Audited
    public ResponseEntity<ObservationGroupDTO> createObservationGroup(Long studyId, ObservationGroupDTO observationGroupDTO) {
        observationGroupDTO.setStudyId(studyId);
        ObservationGroup observationGroup = service.createObservationGroup(
                ObservationGroupTransformer.fromObservationGroupDTO_V1(observationGroupDTO)
        );
        LOGGER.debug("ObservationGroup created: {}", observationGroup);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ObservationGroupTransformer.toObservationGroupDTO_V1(observationGroup)
        );
    }

    @Override
    @RequiresStudyRole
    @Audited
    public ResponseEntity<List<ObservationGroupDTO>> listObservationGroups(Long studyId) {
        return ResponseEntity.ok(
                service.listObservationGroups(studyId).stream()
                        .map(ObservationGroupTransformer::toObservationGroupDTO_V1)
                        .toList()
        );
    }

    @Override
    @RequiresStudyRole
    @Audited
    public ResponseEntity<ObservationGroupDTO> getObservationGroup(Long studyId, Integer observationGroupId) {
        return ResponseEntity.ok(
                ObservationGroupTransformer.toObservationGroupDTO_V1(service.getObservationGroup(studyId, observationGroupId))
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    @Audited
    public ResponseEntity<ObservationGroupDTO> updateObservationGroup(Long studyId, Integer observationGroupId, ObservationGroupDTO observationGroupDTO) {
        observationGroupDTO.setStudyId(studyId);
        observationGroupDTO.setObservationGroupId(observationGroupId);
        return ResponseEntity.ok(
                ObservationGroupTransformer.toObservationGroupDTO_V1(
                        service.updateObservationGroup(
                                ObservationGroupTransformer.fromObservationGroupDTO_V1(observationGroupDTO)
                        )
                )
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    @Audited
    public ResponseEntity<Void> deleteObservationGroup(Long studyId, Integer observationGroupId) {
        service.deleteObservationGroup(studyId, observationGroupId);
        return ResponseEntity.noContent().build();
    }
}
