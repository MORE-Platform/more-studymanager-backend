package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.ObservationDTO;
import io.redlink.more.studymanager.api.v1.webservices.ObservationsApi;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.transformer.ObservationTransformer;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import io.redlink.more.studymanager.service.ObservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class ObservationsApiV1Controller implements ObservationsApi {

    private final ObservationService service;

    private final OAuth2AuthenticationService authService;


    public ObservationsApiV1Controller(ObservationService service, OAuth2AuthenticationService authService) {
        this.service = service;
        this.authService = authService;
    }

    @Override
    public ResponseEntity<ObservationDTO> addObservation(Long studyId, ObservationDTO observationDTO) {
        final var currentUser = authService.getCurrentUser();
        Observation observation = service.addObservation(
                ObservationTransformer.fromObservationDTO_V1(observationDTO.studyId(studyId)),
                currentUser
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ObservationTransformer.toObservationDTO_V1(observation)
        );
    }

    @Override
    public ResponseEntity<Void> deleteObservation(Long studyId, Integer observationId) {
        final var currentUser = authService.getCurrentUser();
        service.deleteObservation(studyId, observationId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<ObservationDTO>> listObservations(Long studyId) {
        final var currentUser = authService.getCurrentUser();
        return ResponseEntity.ok().body(
                service.listObservations(studyId, currentUser).stream()
                        .map(ObservationTransformer::toObservationDTO_V1)
                        .toList()
        );
    }

    @Override
    public ResponseEntity<ObservationDTO> updateObservation(Long studyId, Integer observationId, ObservationDTO observationDTO) {
        final var currentUser = authService.getCurrentUser();
        Observation observation = service.updateObservation(
                ObservationTransformer.fromObservationDTO_V1(observationDTO.studyId(studyId).observationId(observationId)),
                currentUser
        );
        return ResponseEntity.status(HttpStatus.OK).body(
                ObservationTransformer.toObservationDTO_V1(observation)
        );
    }
}
