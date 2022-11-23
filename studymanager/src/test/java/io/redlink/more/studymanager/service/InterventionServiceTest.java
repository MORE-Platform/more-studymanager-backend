package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.validation.ConfigurationValidationReport;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.core.factory.TriggerFactory;
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
    Map<String, TriggerFactory> triggerFactories;
    @InjectMocks
    InterventionService interventionService;
    @Test
    public void testNotFoundValidation() {
        NotFoundException notFoundException = Assertions.assertThrows(NotFoundException.class, () -> interventionService.updateTrigger(1L, 1, new Trigger().setType("my-trigger")));
        Assertions.assertEquals("Trigger Factory 'my-trigger' cannot be found", notFoundException.getMessage());
    }

    @Test
    public void testBadRequestValidation() {
        TriggerFactory factory = mock(TriggerFactory.class);
        when(factory.validate(any())).thenThrow(new ConfigurationValidationException(ConfigurationValidationReport.VALID.error("My error")));
        when(triggerFactories.get("my-trigger")).thenReturn(factory);
        when(triggerFactories.containsKey("my-trigger")).thenReturn(true);

        Assertions.assertThrows(BadRequestException.class, () -> interventionService.updateTrigger(1L, 1, new Trigger().setType("my-trigger")));
    }

}
