package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.validation.ConfigurationValidationReport;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.repository.ObservationRepository;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObservationServiceTest {

    @Mock
    ObservationRepository observationRepository;

    @Mock
    StudyPermissionService studyPermissionService;

    @Mock
    Map<String, ObservationFactory> observationFactories;

    @InjectMocks
    ObservationService observationService;

    @Test
    void testValidation() {
        NotFoundException notFoundException = Assertions.assertThrows(NotFoundException.class, () ->
                observationService.addObservation(new Observation().setStudyId(1L).setObservationId(1).setType("my-observation"))
        );
        Assertions.assertEquals("Observation Factory 'my-observation' cannot be found", notFoundException.getMessage());

        ObservationFactory factory = mock(ObservationFactory.class);
        when(factory.validate(any())).thenThrow(new ConfigurationValidationException(ConfigurationValidationReport.init().error("My error")));
        when(observationFactories.get("my-observation")).thenReturn(factory);
        when(observationFactories.containsKey("my-observation")).thenReturn(true);

        BadRequestException badRequestException = Assertions.assertThrows(BadRequestException.class, () ->
                observationService.addObservation(new Observation().setStudyId(1L).setObservationId(1).setType("my-observation"))
        );
        Assertions.assertEquals("ConfigurationValidationReport: [ERROR] My error", badRequestException.getMessage());
    }
}
