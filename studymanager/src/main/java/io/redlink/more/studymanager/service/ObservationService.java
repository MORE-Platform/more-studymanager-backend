/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.core.component.Component;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.repository.ObservationRepository;
import io.redlink.more.studymanager.sdk.MoreSDK;
import java.util.EnumSet;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ObservationService {

    private final StudyStateService studyStateService;
    private final ObservationRepository repository;

    private final Map<String, ObservationFactory> observationFactories;
    private final MoreSDK sdk;

    public ObservationService(StudyStateService studyStateService,
                              ObservationRepository repository,
                              Map<String, ObservationFactory> observationFactories,
                              MoreSDK sdk) {
        this.studyStateService = studyStateService;
        this.repository = repository;
        this.observationFactories = observationFactories;
        this.sdk = sdk;
    }

    public Observation addObservation(Observation observation) {
        studyStateService.assertStudyNotInState(observation.getStudyId(), Study.Status.CLOSED);
        return repository.insert(validate(observation));
    }

    public Observation importObservation(Long studyId, Observation observation) {
        Observation validated = validate(observation);
        ObservationFactory factory = factory(validated);
        ObservationProperties props = (ObservationProperties) factory.preImport(validated.getProperties());
        validated.setProperties(props);
        return repository.doImport(studyId, validated);
    }

    public void deleteObservation(Long studyId, Integer observationId) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        repository.deleteObservation(studyId, observationId);
    }

    public Optional<Observation> getObservation(Long studyId, Integer observationId) {
        try {
            return Optional.ofNullable(repository.getById(studyId, observationId));
        } catch (BadRequestException e) {
            return Optional.empty();
        }
    }

    public List<Observation> listObservations(Long studyId) {
        return repository.listObservations(studyId);
    }

    public Observation updateObservation(Observation observation) {
        studyStateService.assertStudyNotInState(observation.getStudyId(), Study.Status.CLOSED);
        return repository.updateObservation(validate(observation));
    }

    public void alignObservationsWithStudyState(Study study){
        if (EnumSet.of(Study.Status.ACTIVE, Study.Status.PREVIEW).contains(study.getStudyState()))
            activateObservationsFor(study);
        else deactivateObservationsFor(study);
    }

    private void activateObservationsFor(Study study){ listObservationsFor(study).forEach(Component::activate); }

    private void deactivateObservationsFor(Study study){ listObservationsFor(study).forEach(Component::deactivate); }

    public List<io.redlink.more.studymanager.core.component.Observation> listObservationsFor(Study study){
        return listObservations(study.getStudyId()).stream()
                .map(observation -> factory(observation)
                        .create(
                                sdk.scopedObservationSDK(observation.getStudyId(), observation.getStudyGroupId(), observation.getObservationId()),
                                observation.getProperties()
                        ))
                .toList();
    }

    private ObservationFactory factory(Observation observation) {
        return observationFactories.get(observation.getType());
    }

    private Observation validate(Observation observation) {
        if(!observationFactories.containsKey(observation.getType())) {
            throw NotFoundException.ObservationFactory(observation.getType());
        }
        try {
            final var factory = factory(observation);
            factory.validate(observation.getProperties());
        } catch (ConfigurationValidationException e) {
            throw new BadRequestException(e.getMessage());
        }
        return observation;
    }
}
