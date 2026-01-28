/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.ParticipantDTO;
import io.redlink.more.studymanager.api.v1.webservices.ParticipantsApi;
import io.redlink.more.studymanager.audit.Audited;
import io.redlink.more.studymanager.controller.RequiresStudyRole;
import io.redlink.more.studymanager.core.datavalidity.ObservationDataState;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.OccurredObservation;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.transformer.ParticipantTransformer;
import io.redlink.more.studymanager.properties.GatewayProperties;
import io.redlink.more.studymanager.service.OccurredObservationService;
import io.redlink.more.studymanager.service.ParticipantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class ParticipantsApiV1Controller implements ParticipantsApi {
    private static final Logger log = LoggerFactory.getLogger(ParticipantsApiV1Controller.class);
    private final ParticipantService service;
    private final OccurredObservationService occurredObservationService;
    private final GatewayProperties gatewayProperties;


    public ParticipantsApiV1Controller(ParticipantService service, OccurredObservationService occurredObservationService, GatewayProperties gatewayProperties) {
        this.service = service;
        this.occurredObservationService = occurredObservationService;
        this.gatewayProperties = gatewayProperties;
    }

    private ParticipantDTO toParticipantDTO(Participant p) {
        return ParticipantTransformer.toParticipantDTO_V1(p, gatewayProperties);
    }
    private ParticipantDTO.DataHealthIndicatorEnum getDataHealthIndicator(long studyId, int participantId) {
        var now = Instant.now();
        try (Stream<OccurredObservation> ooStream = occurredObservationService.streamOccurredObservations(
                studyId, participantId, null,
                true, //we need to know about invalid observations
                EnumSet.complementOf(EnumSet.of(ObservationDataState.COMPLETE)))){ //Do not list COMPLETED Observations as those are GREEN anyways
            var oos = ooStream.toList();
            log.debug("data health for participant {} of study {}: {}", participantId, studyId, oos);
            if(oos.stream().anyMatch(it -> it.dataValid() == Boolean.FALSE || //Observations with invalid data trigger ORANGE
                    now.isAfter(it.end()) || //if the observation occurrence is already in the past ony state other than completed triggers a ORANGE
                    it.dataState() != ObservationDataState.INCOMPLETE)){ //for ongoing observations INCOMPLETE is considered GREEN
                return ParticipantDTO.DataHealthIndicatorEnum.ORANGE;
            } else {
                return ParticipantDTO.DataHealthIndicatorEnum.GREEN;
            }
        }
   }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    @Audited
    public ResponseEntity<List<ParticipantDTO>> createParticipants(Long studyId, List<ParticipantDTO> participantDTO) {
        List<Participant> participants = participantDTO.stream()
                .map(p -> p.studyId(studyId))
                .map(ParticipantTransformer::fromParticipantDTO_V1)
                .map(service::createParticipant)
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(
                participants.stream()
                        .map(this::toParticipantDTO)
                        .toList()
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    @Audited
    public ResponseEntity<List<ParticipantDTO>> updateParticipantList(Long studyId, List<ParticipantDTO> participantDTO) {
        if (participantDTO.stream().anyMatch(p -> p.getParticipantId() == null)) {
            throw new NotFoundException("Participant without id");
        }
        List<Participant> participants = participantDTO.stream()
                .map(p -> p.studyId(studyId))
                .map(ParticipantTransformer::fromParticipantDTO_V1)
                .map(service::updateParticipant)
                .toList();
        return ResponseEntity.ok(
                participants.stream()
                        .map(this::toParticipantDTO)
                        .toList()
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    @Audited
    public ResponseEntity<Void> deleteParticipant(Long studyId, Integer participantId, Boolean includeData) {
        service.deleteParticipant(studyId, participantId, includeData);
        return ResponseEntity.noContent().build();
    }


    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    @Audited
    public ResponseEntity<ParticipantDTO> getParticipant(Long studyId, Integer participantId) {
        return ResponseEntity.ok(
                toParticipantDTO(service.getParticipant(studyId, participantId))
                        .dataHealthIndicator(getDataHealthIndicator(studyId, participantId))
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    @Audited
    public ResponseEntity<List<ParticipantDTO>> listParticipants(Long studyId) {
        return ResponseEntity.ok(
                service.listParticipants(studyId).stream()
                        .map(this::toParticipantDTO)
                        .map(it -> it.dataHealthIndicator(getDataHealthIndicator(studyId, it.getParticipantId())))
                        .toList()
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    @Audited
    public ResponseEntity<ParticipantDTO> updateParticipant(Long studyId, Integer participantId, ParticipantDTO participantDTO) {
        Participant participant = service.updateParticipant(
                ParticipantTransformer.fromParticipantDTO_V1((participantDTO.studyId(studyId)).participantId(participantId))
        );
        return ResponseEntity.status(HttpStatus.OK).body(
                toParticipantDTO(participant)
                        .dataHealthIndicator(getDataHealthIndicator(studyId, participant.getParticipantId()))
        );
    }
}
