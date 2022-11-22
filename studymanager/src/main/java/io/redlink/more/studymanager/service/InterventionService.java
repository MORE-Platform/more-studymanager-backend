package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.core.component.Component;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ActionFactory;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Action;
import io.redlink.more.studymanager.core.factory.TriggerFactory;
import io.redlink.more.studymanager.model.Intervention;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.Trigger;
import io.redlink.more.studymanager.repository.InterventionRepository;
import io.redlink.more.studymanager.sdk.MoreSDK;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class InterventionService {

    private final InterventionRepository repository;

    private final Map<String, ActionFactory> actionFactories;
    private final Map<String, TriggerFactory> triggerFactories;

    private final MoreSDK sdk;

    public InterventionService(InterventionRepository repository, MoreSDK sdk, Map<String, TriggerFactory> triggerFactories, Map<String, ActionFactory> actionFactories) {
        this.repository = repository;
        this.actionFactories = actionFactories;
        this.triggerFactories = triggerFactories;
        this.sdk = sdk;
    }
    public Intervention addIntervention(Intervention intervention) {
        return repository.insert(intervention);
    }

    public List<Intervention> listInterventions(Long studyId) {
        return repository.listInterventions(studyId);
    }

    public Intervention getIntervention(Long studyId, Integer interventionId) {
        return repository.getByIds(studyId, interventionId);
    }

    public void deleteIntervention(Long studyId, Integer interventionId) {
        repository.deleteByIds(studyId, interventionId);
    }

    public Intervention updateIntervention(Intervention intervention) {
        return repository.updateIntervention(intervention);
    }

    public Action createAction(Long studyId, Integer interventionId, Action action) {
        return repository.createAction(studyId, interventionId, validateAction(action));
    }

    public Action getActionByIds(Long studyId, Integer interventionId, Integer actionId) {
        return repository.getActionByIds(studyId, interventionId, actionId);
    }

    public List<Action> listActions(Long studyId, Integer interventionId) {
        return repository.listActions(studyId, interventionId);
    }

    public void deleteAction(Long studyId, Integer interventionId, Integer actionId) {
        repository.deleteActionByIds(studyId, interventionId, actionId);
    }

    public Action updateAction(Long studyId, Integer interventionId, Integer actionId, Action action) {
        return repository.updateAction(studyId, interventionId, actionId, validateAction(action));
    }

    public Trigger updateTrigger(Long studyId, Integer interventionId, Trigger trigger) {
        return repository.updateTrigger(studyId, interventionId, validateTrigger(trigger));
    }

    public Trigger getTriggerByIds(Long studyId, Integer interventionId) {
        return repository.getTriggerByIds(studyId, interventionId);
    }

    public void activateInterventionsFor(Study study) {
        listTriggersFor(study).forEach(Component::activate);
    }

    public void deactivateInterventionsFor(Study study) {
        listTriggersFor(study).forEach(Component::deactivate);
    }

    private List<io.redlink.more.studymanager.core.component.Trigger> listTriggersFor(Study study) {
        return listInterventions(study.getStudyId()).stream()
                .map(intervention -> {
                    Trigger trigger = getTriggerByIds(intervention.getStudyId(), intervention.getInterventionId());
                    return factory(trigger).create(
                            sdk.scopedTriggerSDK(intervention.getStudyId(), intervention.getStudyGroupId()),
                            trigger.getProperties()
                    );
                }).toList();
    }

    private Action validateAction(Action action) {
        if(!actionFactories.containsKey(action.getType())) {
            throw NotFoundException.ActionFactory(action.getType());
        }
        try {
            factory(action).validate(action.getProperties());
        } catch (ConfigurationValidationException e) {
            throw new BadRequestException(e.getMessage());
        }
        return action;
    }

    private Trigger validateTrigger(Trigger trigger) {
        if(!triggerFactories.containsKey(trigger.getType())) {
            throw NotFoundException.TriggerFactory(trigger.getType());
        }
        try {
            factory(trigger).validate(trigger.getProperties());
        } catch (ConfigurationValidationException e) {
            throw new BadRequestException(e.getMessage());
        }
        return trigger;
    }

    private TriggerFactory factory(Trigger trigger) {
        return triggerFactories.get(trigger.getType());
    }

    private ActionFactory factory(Action action) {
        return actionFactories.get(action.getType());
    }
}
