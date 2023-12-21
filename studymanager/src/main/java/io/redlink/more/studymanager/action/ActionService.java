/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.action;

import io.redlink.more.studymanager.core.component.Action;
import io.redlink.more.studymanager.core.factory.ActionFactory;
import io.redlink.more.studymanager.core.io.ActionParameter;
import io.redlink.more.studymanager.sdk.MoreSDK;
import io.redlink.more.studymanager.service.InterventionService;
import io.redlink.more.studymanager.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class ActionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionService.class);

    private final InterventionService interventionService;

    private final Map<String, ActionFactory> actionFactories;

    private final MoreSDK moreSDK;

    private final ActionWorker worker;

    public ActionService(
            InterventionService interventionService,
            Map<String, ActionFactory> actionFactories,
            MoreSDK moreSDK,
            ActionWorker worker) {
        this.interventionService = interventionService;
        this.actionFactories = actionFactories;
        this.moreSDK = moreSDK;
        this.worker = worker;
    }

    public void execute(long studyId, Integer studyGroupId, int interventionId, Set<ActionParameter> parameters) {
        if (parameters == null) {
            return;
        }
        this.interventionService.listActions(studyId, interventionId)
                .forEach(action -> executeAction(studyId, studyGroupId, interventionId, parameters, action));
    }

    private void executeAction(long studyId, Integer studyGroupId, int interventionId, Set<ActionParameter> parameters,
                               io.redlink.more.studymanager.model.Action action) {
        try (var ctx = LoggingUtils.createContext()) {
            ctx.putAction(action);
            ActionFactory factory = actionFactories.get(action.getType());

            if (factory == null) {
                LOGGER.error("Skipping action_{} from intervention_{} in study_{}: No factory found for actionType {}",
                        action.getActionId(), interventionId, studyId, action.getType());
                return;
            }

            parameters.forEach(parameter -> {
                ctx.putParticipant(parameter.getParticipantId());
                Action executable = factory.create(
                        moreSDK.scopedActionSDK(
                                studyId, studyGroupId, interventionId, action.getActionId(), action.getType(), parameter.getParticipantId()
                        ),
                        action.getProperties()
                );
                try {
                    worker.execute(executable, parameter);
                } catch (Exception e) {
                    LOGGER.warn("Error executing action_{} [{}] from intervention_{} in study_{}: {}",
                            action.getActionId(), action.getType(), interventionId, studyId, e.getMessage(), e);
                }
            });
        }
    }

}
