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
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.Trigger;
import io.redlink.more.studymanager.model.User;
import io.redlink.more.studymanager.repository.InterventionRepository;
import io.redlink.more.studymanager.repository.StudyRepository;
import io.redlink.more.studymanager.sdk.MoreSDK;
import io.redlink.more.studymanager.utils.LoggingUtils;
import java.util.EnumSet;
import java.util.Set;
import org.slf4j.MDC;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class InterventionService {

    private static final Set<StudyRole> READ_ROLES = EnumSet.of(StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR);
    private static final Set<StudyRole> WRITE_ROLES = READ_ROLES;

    private final InterventionRepository repository;

    private final StudyRepository studyRepository;

    private final StudyPermissionService studyPermissionService;

    private final Map<String, ActionFactory> actionFactories;
    private final Map<String, TriggerFactory> triggerFactories;

    private final MoreSDK sdk;

    public InterventionService(InterventionRepository repository, StudyRepository studyRepository,
                               StudyPermissionService studyPermissionService,
                               MoreSDK sdk,
                               Map<String, TriggerFactory> triggerFactories,
                               Map<String, ActionFactory> actionFactories) {
        this.repository = repository;
        this.studyRepository = studyRepository;
        this.studyPermissionService = studyPermissionService;
        this.actionFactories = actionFactories;
        this.triggerFactories = triggerFactories;
        this.sdk = sdk;
    }

    public Intervention addIntervention(Intervention intervention, User user) {
        studyPermissionService.assertAnyRole(intervention.getStudyId(), user.id(), WRITE_ROLES);
        return repository.insert(intervention);
    }

    public List<Intervention> listInterventions(Long studyId, User user) {
        if (user != null) {
            studyPermissionService.assertAnyRole(studyId, user.id(), READ_ROLES);
        }
        return repository.listInterventions(studyId);
    }

    public Intervention getIntervention(Long studyId, Integer interventionId, User user) {
        studyPermissionService.assertAnyRole(studyId, user.id(), READ_ROLES);
        return repository.getByIds(studyId, interventionId);
    }

    public void deleteIntervention(Long studyId, Integer interventionId, User user) {
        studyPermissionService.assertAnyRole(studyId, user.id(), WRITE_ROLES);
        repository.deleteByIds(studyId, interventionId);
    }

    public Intervention updateIntervention(Intervention intervention, User user) {
        studyPermissionService.assertAnyRole(intervention.getStudyId(), user.id(), WRITE_ROLES);
        return repository.updateIntervention(intervention);
    }

    public Action createAction(Long studyId, Integer interventionId, Action action, User user) {
        studyPermissionService.assertAnyRole(studyId, user.id(), WRITE_ROLES);
        return repository.createAction(studyId, interventionId, validateAction(action));
    }

    public Action getActionByIds(Long studyId, Integer interventionId, Integer actionId, User user) {
        studyPermissionService.assertAnyRole(studyId, user.id(), READ_ROLES);
        return repository.getActionByIds(studyId, interventionId, actionId);
    }

    public List<Action> listActions(Long studyId, Integer interventionId, User user) {
        if (user != null) {
            studyPermissionService.assertAnyRole(studyId, user.id(), READ_ROLES);
        }
        return repository.listActions(studyId, interventionId);
    }

    public void deleteAction(Long studyId, Integer interventionId, Integer actionId, User user) {
        studyPermissionService.assertAnyRole(studyId, user.id(), WRITE_ROLES);
        repository.deleteActionByIds(studyId, interventionId, actionId);
    }

    public Action updateAction(Long studyId, Integer interventionId, Integer actionId, Action action, User user) {
        studyPermissionService.assertAnyRole(studyId, user.id(), WRITE_ROLES);
        return repository.updateAction(studyId, interventionId, actionId, validateAction(action));
    }

    public Trigger updateTrigger(Long studyId, Integer interventionId, Trigger trigger, User user) {
        studyPermissionService.assertAnyRole(studyId, user.id(), WRITE_ROLES);
        return repository.updateTrigger(studyId, interventionId, validateTrigger(trigger));
    }

    public Trigger getTriggerByIds(Long studyId, Integer interventionId, User user) {
        if (user != null) {
            studyPermissionService.assertAnyRole(studyId, user.id(), READ_ROLES);
        }
        return repository.getTriggerByIds(studyId, interventionId);
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onStartUp() {
        studyRepository.listStudiesByStatus(Study.Status.ACTIVE).forEach(study -> {
            try (var ctx = LoggingUtils.createContext(study)) {
                activateInterventionsFor(study);
            }
        });
    }

    public void alignInterventionsWithStudyState(Study study) {
        if (study.getStudyState() == Study.Status.ACTIVE) {
            activateInterventionsFor(study);
        } else {
            deactivateInterventionsFor(study);
        }
    }

    public void activateInterventionsFor(Study study) {
        listTriggersFor(study).forEach(Component::activate);
    }

    public void deactivateInterventionsFor(Study study) {
        listTriggersFor(study).forEach(Component::deactivate);
    }

    private List<io.redlink.more.studymanager.core.component.Trigger> listTriggersFor(Study study) {
        return listInterventions(study.getStudyId(), null).stream()
                .map(intervention -> Optional.ofNullable(
                                getTriggerByIds(intervention.getStudyId(), intervention.getInterventionId(), null))
                        .map(trigger -> factory(trigger)
                                .create(
                                    sdk.scopedTriggerSDK(intervention.getStudyId(), intervention.getStudyGroupId(), intervention.getInterventionId()),
                                    trigger.getProperties()
                                )
                        ).orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    private Action validateAction(Action action) {
        if (!actionFactories.containsKey(action.getType())) {
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
        if (!triggerFactories.containsKey(trigger.getType())) {
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
