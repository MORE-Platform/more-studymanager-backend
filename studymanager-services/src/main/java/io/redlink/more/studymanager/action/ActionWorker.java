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
import io.redlink.more.studymanager.core.io.ActionParameter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ActionWorker {

    //@Async
    public void execute(Action action, ActionParameter parameter) {
        action.execute(parameter);
    }
}
