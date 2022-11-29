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
