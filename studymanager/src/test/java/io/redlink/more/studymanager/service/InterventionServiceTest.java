package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.factory.TriggerFactory;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.core.validation.ConfigurationValidationReport;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.Trigger;
import io.redlink.more.studymanager.repository.InterventionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InterventionServiceTest {

    @Mock
    InterventionRepository interventionRepository;

    @Mock
    Map<String, TriggerFactory> triggerFactories;

    @Mock
    MoreTriggerSDK sdk;

    @InjectMocks
    InterventionService interventionService;

    @Test
    public void testValidation() {
        NotFoundException notFoundException = Assertions.assertThrows(NotFoundException.class, () -> {
            interventionService.updateTrigger(1L, 1, new Trigger().setType("my-trigger"));
        });
        Assertions.assertEquals("Trigger Factory 'my-trigger' cannot be found", notFoundException.getMessage());

        TriggerFactory factory = mock(TriggerFactory.class);
        when(factory.create(any(), any())).thenThrow(new ConfigurationValidationException(ConfigurationValidationReport.VALID.error("My error")));
        when(triggerFactories.get("my-trigger")).thenReturn(factory);
        when(triggerFactories.containsKey("my-trigger")).thenReturn(true);

        BadRequestException badRequestException = Assertions.assertThrows(BadRequestException.class, () -> {
            interventionService.updateTrigger(1L, 1, new Trigger().setType("my-trigger"));
        });
        Assertions.assertEquals("ConfigurationValidationReport: [ERROR] My error", badRequestException.getMessage());
    }
}
