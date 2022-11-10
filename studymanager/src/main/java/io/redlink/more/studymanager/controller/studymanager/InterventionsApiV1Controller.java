package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.ActionDTO;
import io.redlink.more.studymanager.api.v1.model.InterventionDTO;
import io.redlink.more.studymanager.api.v1.model.TriggerDTO;
import io.redlink.more.studymanager.api.v1.webservices.InterventionsApi;
import io.redlink.more.studymanager.model.transformer.InterventionTransformer;
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
    public ResponseEntity<InterventionDTO> addIntervention(Long studyId, InterventionDTO interventionDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                InterventionTransformer.toInterventionDTO_V1(
                        service.addIntervention(
                                InterventionTransformer.fromInterventionDTO_V1(interventionDTO.studyId(studyId))))
        );
    }

    @Override
    public ResponseEntity<ActionDTO> createAction(Long studyId, Integer interventionId, ActionDTO actionDTO) {
        // TODO in MORE-261
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteAction(Long studyId, Integer interventionId, Integer actionId) {
        // TODO in MORE-261
        return null;
    }

    @Override
    public ResponseEntity<ActionDTO> getAction(Long studyId, Integer interventionId, Integer actionId) {
        // TODO in MORE-261
        return null;
    }

    @Override
    public ResponseEntity<InterventionDTO> getIntervention(Long studyId, Integer interventionId) {
        return ResponseEntity.status(HttpStatus.OK).body(
                InterventionTransformer.toInterventionDTO_V1(service.getIntervention(studyId, interventionId))
        );
    }

    @Override
    public ResponseEntity<TriggerDTO> getTrigger(Long studyId, Integer interventionId) {
        // TODO in MORE-260
        return null;
    }

    @Override
    public ResponseEntity<List<ActionDTO>> listActions(Long studyId, Integer interventionId) {
        // TODO in MORE-261
        return null;
    }

    @Override
    public ResponseEntity<List<InterventionDTO>> listInterventions(Long studyId) {
        return ResponseEntity.status(HttpStatus.OK).body(
                service.listInterventions(studyId).stream().map(InterventionTransformer::toInterventionDTO_V1).toList()
        );
    }

    @Override
    public ResponseEntity<ActionDTO> updateAction(Long studyId, Integer interventionId, Integer actionId, ActionDTO actionDTO) {
        // TODO in MORE-261
        return null;
    }

    @Override
    public ResponseEntity<TriggerDTO> updateTrigger(Long studyId, Integer interventionId, TriggerDTO triggerDTO) {
        // TODO in MORE-260
        return null;
    }
}
