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
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.transformer.ParticipantTransformer;
import io.redlink.more.studymanager.properties.GatewayProperties;
import io.redlink.more.studymanager.service.ParticipantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class ParticipantsApiV1Controller implements ParticipantsApi {
    private final ParticipantService service;
    private final GatewayProperties gatewayProperties;


    public ParticipantsApiV1Controller(ParticipantService service, GatewayProperties gatewayProperties) {
        this.service = service;
        this.gatewayProperties = gatewayProperties;
    }

    private ParticipantDTO toParticipantDTO(Participant p) {
        return ParticipantTransformer.toParticipantDTO_V1(p, gatewayProperties);
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
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    @Audited
    public ResponseEntity<List<ParticipantDTO>> listParticipants(Long studyId) {
        return ResponseEntity.ok(
                service.listParticipants(studyId).stream()
                        .map(this::toParticipantDTO)
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
        );
    }
}
