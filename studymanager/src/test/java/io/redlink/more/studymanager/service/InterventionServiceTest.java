package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ActionFactory;
import io.redlink.more.studymanager.core.sdk.MoreActionSDK;
import io.redlink.more.studymanager.core.validation.ConfigurationValidationReport;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Action;
import io.redlink.more.studymanager.core.factory.TriggerFactory;
import io.redlink.more.studymanager.core.validation.ConfigurationValidationReport;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Trigger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InterventionServiceTest {
    @Mock
    Map<String, ActionFactory> actionFactories;

    @Mock
    MoreActionSDK sdk;

    @Mock
    Map<String, TriggerFactory> triggerFactories;
    @InjectMocks
    InterventionService interventionService;

    @Test
    public void testActionValidation() {
        NotFoundException notFoundException = Assertions.assertThrows(NotFoundException.class, () -> {
            interventionService.createAction(1L, 1, new Action().setType("my-action"));
        });
        Assertions.assertEquals("Action Factory 'my-action' cannot be found", notFoundException.getMessage());

        notFoundException = Assertions.assertThrows(NotFoundException.class, () -> {
            interventionService.updateAction(1L, 1, 1, new Action().setType("my-action"));
        });
        Assertions.assertEquals("Action Factory 'my-action' cannot be found", notFoundException.getMessage());


        ActionFactory factory = mock(ActionFactory.class);
        when(factory.create(any(), any())).thenThrow(new ConfigurationValidationException(ConfigurationValidationReport.VALID.error("My error")));
        when(actionFactories.get("my-action")).thenReturn(factory);
        when(actionFactories.containsKey("my-action")).thenReturn(true);

        Assertions.assertThrows(BadRequestException.class, () -> {
            interventionService.createAction(1L, 1, new Action().setType("my-action"));
        });
    }

    @Test
    public void testTriggerValidation() {
        NotFoundException notFoundException = Assertions.assertThrows(NotFoundException.class, () -> interventionService.updateTrigger(1L, 1, new Trigger().setType("my-trigger")));
        Assertions.assertEquals("Trigger Factory 'my-trigger' cannot be found", notFoundException.getMessage());

        TriggerFactory factory = mock(TriggerFactory.class);
        when(factory.create(any(), any())).thenThrow(new ConfigurationValidationException(ConfigurationValidationReport.VALID.error("My error")));
        when(triggerFactories.get("my-trigger")).thenReturn(factory);
        when(triggerFactories.containsKey("my-trigger")).thenReturn(true);

        Assertions.assertThrows(BadRequestException.class, () -> interventionService.updateTrigger(1L, 1, new Trigger().setType("my-trigger")));
    }
}
