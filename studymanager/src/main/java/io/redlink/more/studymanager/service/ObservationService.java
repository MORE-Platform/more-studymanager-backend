package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.repository.ObservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ObservationService {

    private final ObservationRepository repository;

    public ObservationService(ObservationRepository repository) {
        this.repository = repository;
    }

    public Observation addObservation(Observation observation) {
        return repository.insert(observation);
    }

    public void deleteObservation(Long studyId, Integer observationId) {
        repository.deleteObservation(studyId, observationId);
    }

    public List<Observation> listObservations(Long studyId) {
        return repository.listObservations(studyId);
    }

    public Observation updateObservation(Long studyId, Integer observationId, Observation observation) {
        return null;
    }
}
