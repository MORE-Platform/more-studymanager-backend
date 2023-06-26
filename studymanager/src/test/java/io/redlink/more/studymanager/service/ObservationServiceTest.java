package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.component.observation.*;
import io.redlink.more.studymanager.component.observation.lime.LimeSurveyObservationFactory;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.validation.ConfigurationValidationReport;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.repository.ObservationRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Mock
    StudyStateService studyStateService;

    @Mock
    ScheduleService scheduleService;

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

    @Test
    void testHidden() {
        Observation acc = new Observation()
                .setType("acc-mobile-observation")
                .setHidden(null);
        Observation pol = new Observation()
                .setType("polar-verity-observation")
                .setHidden(null);
        Observation gps = new Observation()
                .setType("gps-mobile-observation")
                .setHidden(null);
        Observation lim = new Observation()
                .setType("lime-survey-observation")
                .setHidden(null);
        Observation qst = new Observation()
                .setType("question-observation")
                .setHidden(null);
        Observation ext = new Observation()
                .setType("external-observation")
                .setHidden(null);

        Observation qst2 = new Observation()
                .setType("question-observation");
        Observation qst3 = new Observation()
                .setType("question-observation")
                .setHidden(true);

        AccMobileObservationFactory accFactory = new AccMobileObservationFactory();
        PolarVerityObservationFactory polFactory = new PolarVerityObservationFactory();
        GpsMobileObservationFactory gpsFactory = new GpsMobileObservationFactory();
        LimeSurveyObservationFactory limFactory = new LimeSurveyObservationFactory();
        QuestionObservationFactory qstFactory = new QuestionObservationFactory();
        ExternalObservationFactory extFactory = new ExternalObservationFactory();

        assertThat(accFactory.getHidden(acc.getHidden())).isTrue();
        assertThat(polFactory.getHidden(pol.getHidden())).isTrue();
        assertThat(gpsFactory.getHidden(gps.getHidden())).isTrue();
        assertThat(limFactory.getHidden(lim.getHidden())).isFalse();
        assertThat(qstFactory.getHidden(qst.getHidden())).isFalse();
        assertThat(extFactory.getHidden(ext.getHidden())).isTrue();

        assertThat(qstFactory.getHidden(qst2.getHidden())).isFalse();
        assertThat(qstFactory.getHidden(qst3.getHidden())).isFalse();
    }
}
