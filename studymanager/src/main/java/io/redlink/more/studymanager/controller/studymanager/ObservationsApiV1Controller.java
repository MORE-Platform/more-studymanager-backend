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
import io.redlink.more.studymanager.api.v1.model.ObservationDTO;
import io.redlink.more.studymanager.api.v1.webservices.ObservationsApi;
import io.redlink.more.studymanager.controller.RequiresStudyRole;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.model.EndpointToken;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.transformer.EndpointTokenTransformer;
import io.redlink.more.studymanager.model.transformer.ObservationTransformer;
import io.redlink.more.studymanager.service.IntegrationService;
import io.redlink.more.studymanager.service.ObservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class ObservationsApiV1Controller implements ObservationsApi {

    private final ObservationService service;

    private final IntegrationService integrationService;

    public ObservationsApiV1Controller(ObservationService service, IntegrationService integrationService) {
        this.service = service;
        this.integrationService = integrationService;
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<ObservationDTO> addObservation(Long studyId, ObservationDTO observationDTO) {
        Observation observation = service.addObservation(
                ObservationTransformer.fromObservationDTO_V1(observationDTO.studyId(studyId))
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ObservationTransformer.toObservationDTO_V1(observation)
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<Void> deleteObservation(Long studyId, Integer observationId) {
        service.deleteObservation(studyId, observationId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<List<ObservationDTO>> listObservations(Long studyId) {
        return ResponseEntity.ok().body(
                service.listObservations(studyId).stream()
                        .map(ObservationTransformer::toObservationDTO_V1)
                        .toList()
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<ObservationDTO> updateObservation(Long studyId, Integer observationId, ObservationDTO observationDTO) {
        Observation observation = service.updateObservation(
                ObservationTransformer.fromObservationDTO_V1(observationDTO.studyId(studyId).observationId(observationId))
        );
        return ResponseEntity.status(HttpStatus.OK).body(
                ObservationTransformer.toObservationDTO_V1(observation)
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<EndpointTokenDTO> createToken(Long studyId, Integer observationId, String tokenLabel) {
        if(tokenLabel.isBlank()) { return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); }

        Optional<EndpointToken> addedToken = integrationService.addToken(studyId, observationId, tokenLabel.replace("\"", ""));
        if(addedToken.isEmpty()) {
            throw new BadRequestException("Token with given label already exists for given observation");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
                EndpointTokenTransformer.toEndpointTokenDTO(
                        addedToken.get()
                )
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<EndpointTokenDTO> updateTokenLabel(Long studyId, Integer observationId, Integer tokenId, String tokenLabel) {
        Optional<EndpointToken> token = integrationService.updateToken(studyId, observationId, tokenId, tokenLabel.replace("\"", ""));

        if(token.isEmpty()) {
            throw new BadRequestException("Token with given id doesn't exist for given observation");
        }

        return ResponseEntity.ok().body(
                EndpointTokenTransformer.toEndpointTokenDTO(
                        token.get()
                )
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<EndpointTokenDTO> getToken(Long studyId, Integer observationId, Integer tokenId) {

        Optional<EndpointToken> token = integrationService.getToken(studyId, observationId, tokenId);
        if(token.isEmpty()) {
            throw new BadRequestException("Token with given id doesn't exist for given observation");
        }
        return ResponseEntity.ok().body(
                EndpointTokenTransformer.toEndpointTokenDTO(
                        token.get()
                )
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<List<EndpointTokenDTO>> getTokens(Long studyId, Integer observationId) {
        return ResponseEntity.ok().body(
                EndpointTokenTransformer.toEndpointTokensDTO(
                        integrationService.getTokens(studyId, observationId)
                )
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<Void> deleteToken(Long studyId, Integer observationId, Integer tokenId) {
        integrationService.deleteToken(studyId, observationId, tokenId);
        return ResponseEntity.noContent().build();
    }
}
