/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.event.StudyStateChangedEvent;
import io.redlink.more.studymanager.core.component.Component;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.io.TimeRange;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.ui.DataView;
import io.redlink.more.studymanager.core.ui.DataViewInfo;
import io.redlink.more.studymanager.core.validation.ValidationIssue;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.ParticipantWithObservationProperties;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.repository.ObservationRepository;
import io.redlink.more.studymanager.sdk.MoreSDK;
import io.redlink.more.studymanager.utils.RandomSchedulerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ObservationService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final StudyStateService studyStateService;
    private final ObservationRepository repository;

    private final MoreSDK sdk;
    ApplicationContext applicationContext;

    public ObservationService(StudyStateService studyStateService,
                              ObservationRepository repository,
                              MoreSDK sdk,
                              ApplicationContext applicationContext) {
        this.studyStateService = studyStateService;
        this.repository = repository;
        //this.observationFactories = observationFactories;
        this.sdk = sdk;
        this.applicationContext = applicationContext;
    }

    public Observation addObservation(Observation observation) {
        studyStateService.assertStudyNotInState(observation.getStudyId(), Study.Status.CLOSED);
        return repository.insert(validate(observation));
    }

    public Observation importObservation(Long studyId, Observation observation) {
        final ObservationFactory factory = factory(observation);
        if (factory == null) {
            throw NotFoundException.ObservationFactory(observation.getType());
        }
        ObservationProperties props = (ObservationProperties) factory.preImport(observation.getProperties());
        observation.setProperties(props);
        return repository.doImport(studyId, observation);
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

    public List<Observation> listObservationsForGroup(Long studyId, Integer studyGroupId, Collection<Integer> observationGroupIds) {
        return repository.listObservationsForGroup(studyId, studyGroupId, observationGroupIds);
    }

    public Observation updateObservation(Observation observation) {
        studyStateService.assertStudyNotInState(observation.getStudyId(), Study.Status.CLOSED);
        Observation updatedObservation = repository.updateObservation(validate(observation));
        repository.removeParticipantsPropertyKey(observation.getStudyId(), observation.getObservationId(), RandomSchedulerUtils.OBSERVATION_SCHEDULE_SEED_KEY);
        return updatedObservation;
    }

    @EventListener
    public void handleStudyStateChange(StudyStateChangedEvent event) {
        alignObservationsWithStudyState(event.getStudy());
    }

    protected void alignObservationsWithStudyState(Study study) {
        if (Study.Status.ACTIVE_STATES.contains(study.getStudyState()))
            activateObservationsFor(study);
        else deactivateObservationsFor(study);
    }

    private void activateObservationsFor(Study study) {
        listObservationsFor(study).forEach(Component::activate);
    }

    private void deactivateObservationsFor(Study study) {
        listObservationsFor(study).forEach(Component::deactivate);
    }

    private void validateProperties(List<Observation> observations) {
        for (Observation observation : observations) {
            try {
                factory(observation).validate(observation.getProperties());
            } catch (ConfigurationValidationException e) {
                for (ValidationIssue issue : e.getReport().getIssues()) {
                    issue.setComponentTitle(observation.getTitle());
                }
                throw e;
            }
        }
    }

    public List<io.redlink.more.studymanager.core.component.Observation> listObservationsFor(Study study) {
        List<Observation> observations = listObservations(study.getStudyId());
        List<io.redlink.more.studymanager.core.component.Observation> result = new ArrayList<>();

        validateProperties(observations);

        for (Observation observation : observations) {
            result.add(factory(observation).create(
                sdk.scopedObservationSDK(observation.getStudyId(), observation.getStudyGroupId(), observation.getObservationId()),
                observation.getProperties()));
        }

        return result;
    }

    public DataViewInfo[] listDataViews(Long studyId, Integer observationId) {
        var obs = getObservation(studyId, observationId).orElseThrow();

        return factory(obs).create(
                sdk.scopedObservationSDK(obs.getStudyId(), obs.getStudyGroupId(), obs.getObservationId()), obs.getProperties()
        ).listViews();
    }

    public DataView queryData(Long studyId, Integer observationId, String viewName, Integer studyGroupId, Integer participantId, TimeRange timerange) {
        var obs = getObservation(studyId, observationId).orElseThrow();

        return factory(obs).create(
                sdk.scopedObservationSDK(obs.getStudyId(), obs.getStudyGroupId(), obs.getObservationId()), obs.getProperties()
        ).getView(viewName, studyGroupId, participantId, timerange);
    }

    public List<ParticipantWithObservationProperties> getParticipantObservationProperties(Long studyId) {
        return repository.getParticipantObservationProperties(studyId);
    }

    public Optional<ObservationFactory> getObservationFactory(Observation observation) {
        return Optional.ofNullable(applicationContext.getBean(observation.getType(), ObservationFactory.class));
    }

    /**
     * Ensures the observationFactory for the parsed Observation
     * @param observation
     * @return the factory
     * @throws NotFoundException if the {@link ObservationFactory} for the parsed observation is not present
     */
    private ObservationFactory factory(Observation observation) {
        return getObservationFactory(observation)
                .orElseThrow(() -> new NotFoundException(String.format("ObservationFactory for Observation[study: %s, id:%s, type: %s]",
                        observation.getStudyId(), observation.getObservationId(), observation.getType())));
    }

    private Observation validate(Observation observation) {
        try {
            factory(observation).validate(observation.getProperties());
        } catch (ConfigurationValidationException e) {
            throw new BadRequestException(e.getMessage());
        }
        return observation;
    }
}
