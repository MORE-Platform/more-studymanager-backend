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
import io.redlink.more.studymanager.core.factory.TriggerFactory;
import io.redlink.more.studymanager.core.validation.ConfigurationValidationReport;
import io.redlink.more.studymanager.event.StudyStateChangedEvent;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.AuthenticatedUser;
import io.redlink.more.studymanager.model.PlatformRole;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.Trigger;
import io.redlink.more.studymanager.repository.IntegrationRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntegrationServiceTest {
    @Mock
    StudyStateService studyStateService;
    @Mock
    IntegrationRepository repository;
    @Mock
    PasswordEncoder passwordEncoder;
    @InjectMocks
    IntegrationService integrationService;

    @Test
    void testHandleStudyStateChange() {
        Study study = new Study()
                .setStudyId(1L)
                .setTitle("Test study")
                .setStudyState(Study.Status.ACTIVE);

        integrationService.handleStudyStateChange(new StudyStateChangedEvent(this,study, Study.Status.DRAFT ));
        Mockito.verify(repository, Mockito.never()).clearForStudyId(anyLong());
        study.setStudyState(Study.Status.CLOSED);
        integrationService.handleStudyStateChange(new StudyStateChangedEvent(this,study, Study.Status.ACTIVE ));
        Mockito.verify(repository, Mockito.times(1)).clearForStudyId(eq(study.getStudyId()));

    }

}
