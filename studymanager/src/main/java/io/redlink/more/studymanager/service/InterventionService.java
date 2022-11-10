package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.model.Intervention;
import io.redlink.more.studymanager.repository.InterventionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InterventionService {

    private final InterventionRepository repository;

    public InterventionService(InterventionRepository repository) {
        this.repository = repository;
    }
    public Intervention addIntervention(Intervention intervention) {
        return repository.insert(intervention);
    }

    public List<Intervention> listInterventions(Long studyId) {
        return repository.listInterventions(studyId);
    }

}