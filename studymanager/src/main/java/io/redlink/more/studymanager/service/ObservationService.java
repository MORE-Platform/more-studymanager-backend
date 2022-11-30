package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.User;
import io.redlink.more.studymanager.repository.ObservationRepository;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ObservationService {

    private static final Set<StudyRole> EDIT_ROLES = EnumSet.of(StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR);

    private final ObservationRepository repository;

    private final StudyPermissionService studyPermissionService;

    private final Map<String, ObservationFactory> observationFactories;

    public ObservationService(ObservationRepository repository, StudyPermissionService studyPermissionService,
                              Map<String, ObservationFactory> observationFactories) {
        this.repository = repository;
        this.studyPermissionService = studyPermissionService;
        this.observationFactories = observationFactories;
    }

    public Observation addObservation(Observation observation, User user) {
        studyPermissionService.assertAnyRole(observation.getStudyId(), user.id(), EDIT_ROLES);

        return repository.insert(validate(observation));
    }

    public void deleteObservation(Long studyId, Integer observationId, User user) {
        studyPermissionService.assertAnyRole(studyId, user.id(), EDIT_ROLES);

        repository.deleteObservation(studyId, observationId);
    }

    public List<Observation> listObservations(Long studyId, User user) {
        studyPermissionService.assertAnyRole(studyId, user.id(), EDIT_ROLES);
        return repository.listObservations(studyId);
    }

    public Observation updateObservation(Observation observation, User user) {
        studyPermissionService.assertAnyRole(observation.getStudyId(), user.id(), EDIT_ROLES);
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
