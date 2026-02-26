/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.EndpointTokenDTO;
import io.redlink.more.studymanager.api.v1.model.ExternalTriggerRequestDTO;
import io.redlink.more.studymanager.controller.RequiresStudyRole;
import io.redlink.more.studymanager.model.EndpointToken;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.transformer.EndpointTokenTransformer;
import io.redlink.more.studymanager.repository.NameValuePairRepository;
import io.redlink.more.studymanager.service.InterventionTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class ApiTriggerController {

    public static final String PENDING_PARTICIPANTS_KEY = "pendingParticipants";

    private final NameValuePairRepository nvpairs;
    private final InterventionTokenService interventionTokenService;

    public ApiTriggerController(NameValuePairRepository nvpairs,
                                InterventionTokenService interventionTokenService) {
        this.nvpairs = nvpairs;
        this.interventionTokenService = interventionTokenService;
    }

    // --- Token management endpoints (OAuth2-protected) ---

    @PostMapping("/studies/{studyId}/interventions/{interventionId}/tokens")
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<EndpointTokenDTO> createToken(
            @PathVariable Long studyId,
            @PathVariable Integer interventionId,
            @RequestBody EndpointTokenDTO endpointTokenDTO) {

        String tokenLabel = endpointTokenDTO.getTokenLabel();
        if (tokenLabel == null || tokenLabel.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        return interventionTokenService.addToken(studyId, interventionId, tokenLabel)
                .map(EndpointTokenTransformer::toEndpointTokenDTO)
                .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto))
                .orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping("/studies/{studyId}/interventions/{interventionId}/tokens")
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<List<EndpointTokenDTO>> getTokens(
            @PathVariable Long studyId,
            @PathVariable Integer interventionId) {

        List<EndpointToken> tokens = interventionTokenService.getTokens(studyId, interventionId);
        return ResponseEntity.ok(EndpointTokenTransformer.toEndpointTokensDTO(tokens));
    }

    @DeleteMapping("/studies/{studyId}/interventions/{interventionId}/tokens/{tokenId}")
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<Void> deleteToken(
            @PathVariable Long studyId,
            @PathVariable Integer interventionId,
            @PathVariable Integer tokenId) {

        interventionTokenService.deleteToken(studyId, interventionId, tokenId);
        return ResponseEntity.noContent().build();
    }

    // --- External trigger endpoint (pre-validated by data-gateway) ---

    @PostMapping("/trigger/external")
    public ResponseEntity<Void> triggerExternal(
            @RequestBody ExternalTriggerRequestDTO request) {

        addPendingParticipants(request.getStudyId(), request.getInterventionId(), request.getParticipantIds());
        return ResponseEntity.accepted().build();
    }

    @SuppressWarnings("unchecked")
    private void addPendingParticipants(Long studyId, Integer interventionId, List<Integer> participantIds) {
        Set<Integer> pending = nvpairs.getTriggerValue(
                studyId, interventionId, PENDING_PARTICIPANTS_KEY, HashSet.class)
                .orElse(new HashSet<>());

        pending.addAll(participantIds);

        nvpairs.setTriggerValue(studyId, interventionId, PENDING_PARTICIPANTS_KEY, (Serializable) pending);
    }
}
