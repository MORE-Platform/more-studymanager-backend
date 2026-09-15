/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.validation.ConfigurationValidationReport;
import io.redlink.more.studymanager.event.StudyStateChangedEvent;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.core.factory.TriggerFactory;
import io.redlink.more.studymanager.model.AuthenticatedUser;
import io.redlink.more.studymanager.model.PlatformRole;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.Trigger;
import java.util.EnumSet;
import java.util.UUID;

import io.redlink.more.studymanager.repository.InterventionRepository;
import io.redlink.more.studymanager.repository.StudyRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterventionServiceTest {
    @Mock
    ApplicationContext applicationContext;
    @Mock
    StudyStateService studyStateService;
    @Mock
    InterventionRepository repository;
    @Mock
    StudyRepository studyRepository;
    @InjectMocks
    InterventionService interventionService;

    private final AuthenticatedUser currentUser = new AuthenticatedUser(
            UUID.randomUUID().toString(),
            "Test User", "test@example.com", "Test Inc.",
            EnumSet.allOf(PlatformRole.class)
    );

    @Test
    void testNotFoundValidation() {
        when(applicationContext.getBean("my-trigger", TriggerFactory.class))
                .thenThrow(new NoSuchBeanDefinitionException("my-trigger"));

        NotFoundException notFoundException = Assertions.assertThrows(NotFoundException.class, () ->
                interventionService.updateTrigger(1L, 1, new Trigger().setType("my-trigger"))
        );
        Assertions.assertEquals("Trigger Factory 'my-trigger' cannot be found", notFoundException.getMessage());
    }

    @Test
    void testBadRequestValidation() {
        TriggerFactory factory = mock(TriggerFactory.class);
        when(factory.validate(any())).thenThrow(new ConfigurationValidationException(ConfigurationValidationReport.init().error("My error")));
        when(applicationContext.getBean("my-trigger", TriggerFactory.class)).thenReturn(factory);

        Assertions.assertThrows(BadRequestException.class, () ->
                interventionService.updateTrigger(1L, 1, new Trigger().setType("my-trigger"))
        );
    }

}
