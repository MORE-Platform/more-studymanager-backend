package io.redlink.more.studymanager.controller.studymanager;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.redlink.more.studymanager.api.v1.model.ObservationDTO;
import io.redlink.more.studymanager.api.v1.webservices.ObservationsApi;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.transformer.ObservationTransformer;
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

    public ObservationsApiV1Controller(ObservationService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<ObservationDTO> addObservation(Long studyId, ObservationDTO observationDTO) {
        Observation observation = service.addObservation(ObservationTransformer.fromObservationDTO_V1(observationDTO.studyId(studyId)));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ObservationTransformer.toObservationDTO_V1(observation)
        );
    }

    @Override
    public ResponseEntity<Void> deleteObservation(Long studyId, Integer observationId) {
        service.deleteObservation(studyId, observationId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<ObservationDTO>> listObservations(Long studyId) {
        return ResponseEntity.ok().body(
                service.listObservations(studyId).stream().map(ObservationTransformer::toObservationDTO_V1).toList()
        );
    }

    @Override
    public ResponseEntity<ObservationDTO> updateObservation(Long studyId, Integer observationId, ObservationDTO observationDTO) throws JsonProcessingException {
        return ResponseEntity.ok(
                ObservationTransformer.toObservationDTO_V1(
                        service.updateObservation(ObservationTransformer.fromObservationDTO_V1(observationDTO))
                )
        );
    }
}
