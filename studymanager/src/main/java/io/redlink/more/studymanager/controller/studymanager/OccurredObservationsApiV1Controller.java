/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.OccurredObservationDTO;
import io.redlink.more.studymanager.api.v1.webservices.OccuredObserrvationsApi;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.model.*;
import io.redlink.more.studymanager.model.transformer.OccurredObservationTransformer;
import io.redlink.more.studymanager.properties.GatewayProperties;
import io.redlink.more.studymanager.service.ObservationService;
import io.redlink.more.studymanager.service.OccurredObservationService;
import io.redlink.more.studymanager.service.ParticipantService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class OccurredObservationsApiV1Controller implements OccuredObserrvationsApi {

    private final OccurredObservationService ooService;
    private final ParticipantService participantService;
    private final ObservationService observationService;
    private final GatewayProperties gatewayProperties;

    public OccurredObservationsApiV1Controller(
            OccurredObservationService ooService,
            ParticipantService participantService,
            ObservationService observationService,
            GatewayProperties gatewayProperties
    ) {
        this.ooService = ooService;
        this.participantService = participantService;
        this.observationService = observationService;
        this.gatewayProperties = gatewayProperties;
    }

    @Override
    public ResponseEntity<List<OccurredObservationDTO>> listOccurredObservations(Long studyId, Integer participant, Integer observation, Instant from, Instant to) {
        if(studyId == null) {
            throw new BadRequestException("studyId is required");
        }
        if(participant == null && observation == null) {
            throw new BadRequestException("either participant or observation is required");
        }
        List<OccurredObservation> occurredObservations;
        try(var ooStream = ooService.streamOccurredObservations(studyId, participant, observation, true, null)) {
            occurredObservations = ooStream.filter(it -> (from == null || it.start().equals(from) || it.start().isAfter(from)) &&
                    (to == null || it.end().equals(to) || it.end().isBefore(to)))
                    //sort for start DESC and end ASC ... this means that the most current OccirredObservations are listed first
                    .sorted(Comparator.comparing(OccurredObservation::start).reversed().thenComparing(OccurredObservation::end))
                    .toList();
        }

        //retrieve all participants and observations referenced by OccurredObservations
        var referencedParticipants = occurredObservations.stream()
                .map(OccurredObservation::participantId)
                .collect(Collectors.toSet()).stream()
                .map(participantId -> participantService.getParticipant(studyId,participantId))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(p -> p.getParticipantId(), Function.identity()));
        var referencedObservations = occurredObservations.stream()
                .map(OccurredObservation::observationId)
                .collect(Collectors.toSet()).stream()
                .map(participantId -> observationService.getObservation(studyId,participantId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(o -> o.getObservationId(), Function.identity()));

        //Convert the data and send the response
        return ResponseEntity.ok(occurredObservations.stream().map( it ->
                new OccurredObservationData(
                        it,
                        referencedParticipants.get(it.participantId()),
                        referencedObservations.get(it.observationId())
                ))
                .map(it -> OccurredObservationTransformer.toOccurredObservationDTO_V1(it, gatewayProperties))
                .toList());
    }

    /**
     * Record that holds data requried to serialize {@link OccurredObservation} as defined in the API
     * @param occurredObservation
     * @param participant
     * @param observation
     */
    public record OccurredObservationData(
            OccurredObservation occurredObservation,
            Participant participant,
            Observation observation
    ){ }
}
