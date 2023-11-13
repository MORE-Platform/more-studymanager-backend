/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.ActionDTO;
import io.redlink.more.studymanager.api.v1.model.InterventionDTO;
import io.redlink.more.studymanager.api.v1.model.TriggerDTO;
import io.redlink.more.studymanager.api.v1.webservices.InterventionsApi;
import io.redlink.more.studymanager.controller.RequiresStudyRole;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.transformer.ActionTransformer;
import io.redlink.more.studymanager.model.transformer.InterventionTransformer;
import io.redlink.more.studymanager.model.transformer.TriggerTransformer;
import io.redlink.more.studymanager.service.InterventionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class InterventionsApiV1Controller implements InterventionsApi {

    private final InterventionService service;

    public InterventionsApiV1Controller(InterventionService service) {
        this.service = service;
    }


    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<InterventionDTO> addIntervention(Long studyId, InterventionDTO interventionDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                InterventionTransformer.toInterventionDTO_V1(
                        service.addIntervention(
                                InterventionTransformer.fromInterventionDTO_V1(interventionDTO.studyId(studyId))
                        )
                )
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<ActionDTO> createAction(Long studyId, Integer interventionId, ActionDTO actionDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ActionTransformer.toActionDTO_V1(
                        service.createAction(
                                studyId, interventionId,
                                ActionTransformer.fromActionDTO_V1(actionDTO)
                        )
                )
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<Void> deleteAction(Long studyId, Integer interventionId, Integer actionId) {
        service.deleteAction(studyId, interventionId, actionId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<Void> deleteIntervention(Long studyId, Integer interventionId) {
        service.deleteIntervention(studyId, interventionId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<ActionDTO> getAction(Long studyId, Integer interventionId, Integer actionId) {
        return ResponseEntity.ok(ActionTransformer.toActionDTO_V1(
                service.getActionByIds(studyId, interventionId, actionId)
        ));
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<InterventionDTO> getIntervention(Long studyId, Integer interventionId) {
        return ResponseEntity.status(HttpStatus.OK).body(InterventionTransformer.toInterventionDTO_V1(
                service.getIntervention(studyId, interventionId)
        ));
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<TriggerDTO> getTrigger(Long studyId, Integer interventionId) {
        return ResponseEntity.ok(TriggerTransformer.toTriggerDTO_V1(
                service.getTriggerByIds(studyId, interventionId)
        ));
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<List<ActionDTO>> listActions(Long studyId, Integer interventionId) {
        return ResponseEntity.ok(service.listActions(studyId, interventionId).stream()
                .map(ActionTransformer::toActionDTO_V1)
                .toList()
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<List<InterventionDTO>> listInterventions(Long studyId) {
        return ResponseEntity.ok(
                service.listInterventions(studyId).stream()
                        .map(InterventionTransformer::toInterventionDTO_V1)
                        .toList()
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<ActionDTO> updateAction(Long studyId, Integer interventionId, Integer actionId, ActionDTO actionDTO) {
        return ResponseEntity.ok(ActionTransformer.toActionDTO_V1(
                service.updateAction(
                        studyId, interventionId, actionId,
                        ActionTransformer.fromActionDTO_V1(actionDTO)
                ))
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<InterventionDTO> updateIntervention(Long studyId, Integer interventionId, InterventionDTO interventionDTO) {
        return ResponseEntity.ok(
                InterventionTransformer.toInterventionDTO_V1(
                        service.updateIntervention(
                                InterventionTransformer.fromInterventionDTO_V1(interventionDTO.studyId(studyId).interventionId(interventionId))
                        )
                )
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<TriggerDTO> updateTrigger(Long studyId, Integer interventionId, TriggerDTO triggerDTO) {
        return ResponseEntity.ok(
                TriggerTransformer.toTriggerDTO_V1(
                        service.updateTrigger(
                                studyId, interventionId,
                                TriggerTransformer.fromTriggerDTO_V1(triggerDTO)
                        )
                )
        );
    }
}
