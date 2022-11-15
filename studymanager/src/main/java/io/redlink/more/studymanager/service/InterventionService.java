package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.TriggerFactory;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Intervention;
import io.redlink.more.studymanager.model.Trigger;
import io.redlink.more.studymanager.repository.InterventionRepository;
import io.redlink.more.studymanager.sdk.MoreTriggerSDKImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class InterventionService {

    private final InterventionRepository repository;

    private final MoreTriggerSDK sdk;

    private final Map<String, TriggerFactory> triggerFactories;

    public InterventionService(InterventionRepository repository, MoreTriggerSDKImpl sdk, Map<String, TriggerFactory> triggerFactories) {
        this.repository = repository;
        this.sdk = sdk;
        this.triggerFactories = triggerFactories;
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

    public Trigger updateTrigger(Long studyId, Integer interventionId, Trigger trigger) {
        return repository.updateTrigger(studyId, interventionId, validate(trigger));
    }

    private Trigger validate(Trigger trigger) {
        if(!triggerFactories.containsKey(trigger.getType())) {
            throw NotFoundException.TriggerFactory(trigger.getType());
        }
        try {
            triggerFactories.get(trigger.getType()).create(sdk, trigger.getProperties());
        } catch (ConfigurationValidationException e) {
            throw new BadRequestException(e.getMessage());
        }
        return trigger;
    }

    public Trigger getTriggerByIds(Long studyId, Integer interventionId) {
        return repository.getTriggerByIds(studyId, interventionId);
    }
}
