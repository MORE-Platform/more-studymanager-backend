/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.repository.NameValuePairRepository;
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

    public ApiTriggerController(NameValuePairRepository nvpairs) {
        this.nvpairs = nvpairs;
    }

    @PostMapping("/studies/{studyId}/interventions/{interventionId}/trigger")
    public ResponseEntity<Void> triggerIntervention(
            @PathVariable Long studyId,
            @PathVariable Integer interventionId,
            @RequestBody TriggerRequest request) {

        // Get existing pending participants (or empty set)
        @SuppressWarnings("unchecked")
        Set<Integer> pending = nvpairs.getTriggerValue(
                studyId, interventionId, PENDING_PARTICIPANTS_KEY, HashSet.class)
                .orElse(new HashSet<>());

        // Add new participant(s)
        pending.addAll(request.participantIds());

        // Store back
        nvpairs.setTriggerValue(studyId, interventionId, PENDING_PARTICIPANTS_KEY, (Serializable) pending);

        return ResponseEntity.accepted().build();
    }

    public record TriggerRequest(List<Integer> participantIds) {}
}
