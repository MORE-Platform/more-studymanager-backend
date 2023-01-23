package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.repository.ObservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ObservationService {

    private final ObservationRepository repository;

    private final Map<String, ObservationFactory> observationFactories;

    public ObservationService(ObservationRepository repository,
                              Map<String, ObservationFactory> observationFactories) {
        this.repository = repository;
        this.observationFactories = observationFactories;
    }

    public Observation addObservation(Observation observation) {
        return repository.insert(validate(observation));
    }

    public void deleteObservation(Long studyId, Integer observationId) {
        repository.deleteObservation(studyId, observationId);
    }

    public List<Observation> listObservations(Long studyId) {
        return repository.listObservations(studyId);
    }

    public Observation updateObservation(Observation observation) {
        return repository.updateObservation(validate(observation));
    }

    private Observation validate(Observation observation) {
        if(!observationFactories.containsKey(observation.getType())) {
            throw NotFoundException.ObservationFactory(observation.getType());
        }
        try {
            observationFactories.get(observation.getType()).validate(observation.getProperties());
        } catch (ConfigurationValidationException e) {
            throw new BadRequestException(e.getMessage());
        }
        return observation;
    }
}
