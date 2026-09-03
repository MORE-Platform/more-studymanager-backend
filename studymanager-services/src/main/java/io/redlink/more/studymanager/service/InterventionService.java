/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.core.component.Component;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ActionFactory;
import io.redlink.more.studymanager.core.factory.TriggerFactory;
import io.redlink.more.studymanager.core.properties.ActionProperties;
import io.redlink.more.studymanager.core.properties.TriggerProperties;
import io.redlink.more.studymanager.event.StudyStateChangedEvent;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Action;
import io.redlink.more.studymanager.model.Intervention;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.Trigger;
import io.redlink.more.studymanager.repository.InterventionRepository;
import io.redlink.more.studymanager.repository.StudyRepository;
import io.redlink.more.studymanager.sdk.MoreSDK;
import io.redlink.more.studymanager.utils.LoggingUtils;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class InterventionService {

    private final StudyStateService studyStateService;
    private final InterventionRepository repository;
    private final StudyRepository studyRepository;
    private final ApplicationContext applicationContext;

    private final MoreSDK sdk;
    private static final Logger LOGGER = LoggerFactory.getLogger(InterventionService.class);


    public InterventionService(StudyStateService studyStateService,
                               InterventionRepository repository, StudyRepository studyRepository,
                               MoreSDK sdk,
                               ApplicationContext applicationContext) {
        this.studyStateService = studyStateService;
        this.repository = repository;
        this.studyRepository = studyRepository;
        this.applicationContext = applicationContext;
        this.sdk = sdk;
    }

    public Intervention addIntervention(Intervention intervention) {
        studyStateService.assertStudyNotInState(intervention.getStudyId(), Study.Status.CLOSED);
        return repository.insert(intervention);
    }

    @Transactional
    public Intervention importIntervention(Long studyId, Intervention intervention, Trigger trigger, List<Action> actions) {
        Intervention imported = repository.importIntervention(studyId, intervention);

        {
            Trigger validated = validateTrigger(trigger);
            TriggerFactory factory = factory(validated)
                    .orElseThrow(() -> NotFoundException.TriggerFactory(trigger.getType()));
            validated.setProperties((TriggerProperties) factory.preImport(validated.getProperties()));
            repository.importTrigger(studyId, imported.getInterventionId(), validateTrigger(validated));
        }

        actions.forEach(a -> {
            Action validated = validateAction(a);
            ActionFactory factory = factory(validated)
                    .orElseThrow(() -> NotFoundException.ActionFactory(a.getType()));
            validated.setProperties((ActionProperties) factory.preImport(validated.getProperties()));
            repository.importAction(studyId, imported.getInterventionId(), validateAction(validated));
        });

        return imported;
    }

    public List<Intervention> listInterventions(Long studyId) {
        return repository.listInterventions(studyId);
    }

    public List<Intervention> listInterventionsForGroup(Long studyId, Integer studyGroupId, Collection<Integer> observationGroupIds) {
        return repository.listInterventionsForGroup(studyId, studyGroupId, observationGroupIds);
    }

    public Intervention getIntervention(Long studyId, Integer interventionId) {
        return repository.getByIds(studyId, interventionId);
    }

    public void deleteIntervention(Long studyId, Integer interventionId) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        repository.deleteByIds(studyId, interventionId);
    }

    public Intervention updateIntervention(Intervention intervention) {
        studyStateService.assertStudyNotInState(intervention.getStudyId(), Study.Status.CLOSED);
        return repository.updateIntervention(intervention);
    }

    public Action createAction(Long studyId, Integer interventionId, Action action) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        return repository.createAction(studyId, interventionId, validateAction(action));
    }

    public Action getActionByIds(Long studyId, Integer interventionId, Integer actionId) {
        return repository.getActionByIds(studyId, interventionId, actionId);
    }

    public List<Action> listActions(Long studyId, Integer interventionId) {
        return repository.listActions(studyId, interventionId);
    }

    public void deleteAction(Long studyId, Integer interventionId, Integer actionId) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        repository.deleteActionByIds(studyId, interventionId, actionId);
    }

    public Action updateAction(Long studyId, Integer interventionId, Integer actionId, Action action) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        return repository.updateAction(studyId, interventionId, actionId, validateAction(action));
    }

    public Trigger updateTrigger(Long studyId, Integer interventionId, Trigger trigger) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        return repository.updateTrigger(studyId, interventionId, validateTrigger(trigger));
    }

    public Trigger getTriggerByIds(Long studyId, Integer interventionId) {
        return repository.getTriggerByIds(studyId, interventionId);
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onStartUp() {
        studyRepository.listStudiesByStatus(Study.Status.ACTIVE).forEach(study -> {
            try (var ctx = LoggingUtils.createContext(study)) {
                activateInterventionsFor(study);
            } catch (RuntimeException e) {
                LOGGER.warn("Failed to activate interventions for study_{}: {}", study.getStudyId(), e.getMessage(), e);
            }
        });
    }

    @EventListener
    public void handleStudyStateChange(StudyStateChangedEvent event) {
        alignInterventionsWithStudyState(event.getStudy());
    }

    private void alignInterventionsWithStudyState(Study study) {
        if (Study.Status.ACTIVE_STATES.contains(study.getStudyState())) {
            activateInterventionsFor(study);
        } else {
            deactivateInterventionsFor(study);
        }
    }

    public void activateInterventionsFor(Study study) {
        listTriggersFor(study).forEach((Component component) -> {
            try (var ctx = LoggingUtils.createContext(study)) {
                component.activate();
                LOGGER.info("Component {} activated", component);
            } catch (RuntimeException e) {
                LOGGER.error("Cannot activate {} study", component, e);
            }
        });
    }

    public void deactivateInterventionsFor(Study study) {
        listTriggersFor(study).forEach(Component::deactivate);
    }

    private List<io.redlink.more.studymanager.core.component.Trigger> listTriggersFor(Study study) {
        return listInterventions(study.getStudyId()).stream()
                .map(intervention -> Optional.ofNullable(
                                getTriggerByIds(intervention.getStudyId(), intervention.getInterventionId()))
                        .map(trigger -> factory(trigger)
                                .orElseThrow(() -> NotFoundException.TriggerFactory(trigger.getType()))
                                .create(
                                        sdk.scopedTriggerSDK(intervention.getStudyId(), intervention.getStudyGroupId(), intervention.getInterventionId(), null),
                                        trigger.getProperties()
                                )
                        ).orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    private Action validateAction(Action action) {
        try {
            factory(action)
                    .orElseThrow(() -> NotFoundException.ActionFactory(action.getType()))
                    .validate(action.getProperties());
        } catch (ConfigurationValidationException e) {
            throw new BadRequestException(e.getMessage());
        }
        return action;
    }

    private Trigger validateTrigger(Trigger trigger) {
        try {
            factory(trigger)
                    .orElseThrow(() -> NotFoundException.TriggerFactory(trigger.getType()))
                    .validate(trigger.getProperties());
            if (trigger.getProperties().containsKey("cronSchedule")) {
                try {
                    CronExpression.validateExpression(trigger.getProperties().get("cronSchedule").toString());
                } catch (ParseException e) {
                    throw ConfigurationValidationException.ofError(e.getMessage());
                }
            }
        } catch (ConfigurationValidationException e) {
            throw new BadRequestException(e.getMessage());
        }
        return trigger;
    }

    private Optional<TriggerFactory> factory(Trigger trigger) {
        try {
            return Optional.of(applicationContext.getBean(trigger.getType(), TriggerFactory.class));
        } catch (NoSuchBeanDefinitionException | BeanNotOfRequiredTypeException e) {
            return Optional.empty();
        }
    }

    private Optional<ActionFactory> factory(Action action) {
        try {
            return Optional.of(applicationContext.getBean(action.getType(), ActionFactory.class));
        } catch (NoSuchBeanDefinitionException | BeanNotOfRequiredTypeException e) {
            return Optional.empty();
        }
    }
}
