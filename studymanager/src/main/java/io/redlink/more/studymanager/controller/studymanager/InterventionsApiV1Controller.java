package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.ActionDTO;
import io.redlink.more.studymanager.api.v1.model.InterventionDTO;
import io.redlink.more.studymanager.api.v1.model.TriggerDTO;
import io.redlink.more.studymanager.api.v1.webservices.InterventionsApi;
import io.redlink.more.studymanager.model.transformer.ActionTransformer;
import io.redlink.more.studymanager.model.transformer.InterventionTransformer;
import io.redlink.more.studymanager.model.transformer.TriggerTransformer;
import io.redlink.more.studymanager.service.InterventionService;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
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

    private final OAuth2AuthenticationService authService;


    public InterventionsApiV1Controller(InterventionService service, OAuth2AuthenticationService authService) {
        this.service = service;
        this.authService = authService;
    }


    @Override
    public ResponseEntity<InterventionDTO> addIntervention(Long studyId, InterventionDTO interventionDTO) {
        final var currentUser = authService.getCurrentUser();
        return ResponseEntity.status(HttpStatus.CREATED).body(
                InterventionTransformer.toInterventionDTO_V1(
                        service.addIntervention(
                                InterventionTransformer.fromInterventionDTO_V1(interventionDTO.studyId(studyId)),
                                currentUser
                        )
                )
        );
    }

    @Override
    public ResponseEntity<ActionDTO> createAction(Long studyId, Integer interventionId, ActionDTO actionDTO) {
        final var currentUser = authService.getCurrentUser();
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ActionTransformer.toActionDTO_V1(
                        service.createAction(
                                studyId, interventionId,
                                ActionTransformer.fromActionDTO_V1(actionDTO),
                                currentUser
                        )
                )
        );
    }

    @Override
    public ResponseEntity<Void> deleteAction(Long studyId, Integer interventionId, Integer actionId) {
        final var currentUser = authService.getCurrentUser();
        service.deleteAction(studyId, interventionId, actionId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> deleteIntervention(Long studyId, Integer interventionId) {
        final var currentUser = authService.getCurrentUser();
        service.deleteIntervention(studyId, interventionId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ActionDTO> getAction(Long studyId, Integer interventionId, Integer actionId) {
        final var currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(ActionTransformer.toActionDTO_V1(
                service.getActionByIds(studyId, interventionId, actionId, currentUser)
        ));
    }

    @Override
    public ResponseEntity<InterventionDTO> getIntervention(Long studyId, Integer interventionId) {
        final var currentUser = authService.getCurrentUser();
        return ResponseEntity.status(HttpStatus.OK).body(InterventionTransformer.toInterventionDTO_V1(
                service.getIntervention(studyId, interventionId, currentUser)
        ));
    }

    @Override
    public ResponseEntity<TriggerDTO> getTrigger(Long studyId, Integer interventionId) {
        final var currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(TriggerTransformer.toTriggerDTO_V1(
                service.getTriggerByIds(studyId, interventionId, currentUser)
        ));
    }

    @Override
    public ResponseEntity<List<ActionDTO>> listActions(Long studyId, Integer interventionId) {
        final var currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(service.listActions(studyId, interventionId, currentUser).stream()
                .map(ActionTransformer::toActionDTO_V1)
                .toList()
        );
    }

    @Override
    public ResponseEntity<List<InterventionDTO>> listInterventions(Long studyId) {
        final var currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(
                service.listInterventions(studyId, currentUser).stream()
                        .map(InterventionTransformer::toInterventionDTO_V1)
                        .toList()
        );
    }

    @Override
    public ResponseEntity<ActionDTO> updateAction(Long studyId, Integer interventionId, Integer actionId, ActionDTO actionDTO) {
        final var currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(ActionTransformer.toActionDTO_V1(
                service.updateAction(
                        studyId, interventionId, actionId,
                        ActionTransformer.fromActionDTO_V1(actionDTO),
                        currentUser
                ))
        );
    }

    @Override
    public ResponseEntity<InterventionDTO> updateIntervention(Long studyId, Integer interventionId, InterventionDTO interventionDTO) {
        final var currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(
                InterventionTransformer.toInterventionDTO_V1(
                        service.updateIntervention(
                                InterventionTransformer.fromInterventionDTO_V1(interventionDTO.studyId(studyId).interventionId(interventionId)),
                                currentUser
                        )
                )
        );
    }

    @Override
    public ResponseEntity<TriggerDTO> updateTrigger(Long studyId, Integer interventionId, TriggerDTO triggerDTO) {
        final var currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(
                TriggerTransformer.toTriggerDTO_V1(
                        service.updateTrigger(
                                studyId, interventionId,
                                TriggerTransformer.fromTriggerDTO_V1(triggerDTO),
                                currentUser
                        )
                )
        );
    }
}
