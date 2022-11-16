package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ActionFactory;
import io.redlink.more.studymanager.core.sdk.MoreActionSDK;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Action;
import io.redlink.more.studymanager.model.Intervention;
import io.redlink.more.studymanager.repository.InterventionRepository;
import io.redlink.more.studymanager.sdk.MoreActionSDKImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class InterventionService {

    private final InterventionRepository repository;

    private final Map<String, ActionFactory> actionFactories;
    private final MoreActionSDK sdk;

    public InterventionService(InterventionRepository repository, Map<String, ActionFactory> actionFactories, MoreActionSDKImpl sdk) {
        this.repository = repository;
        this.actionFactories = actionFactories;
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

    private Action validateAction(Action action) {
        if(!actionFactories.containsKey(action.getType())) {
            throw NotFoundException.ActionFactory(action.getType());
        }
        try {
            actionFactories.get(action.getType()).create(sdk, action.getProperties());
        } catch (ConfigurationValidationException e) {
            throw new BadRequestException(e.getMessage());
        }
        return action;
    }

}
