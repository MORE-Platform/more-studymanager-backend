/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.component.observation.AccMobileObservationFactory;
import io.redlink.more.studymanager.component.observation.ExternalObservationFactory;
import io.redlink.more.studymanager.component.observation.GpsMobileObservationFactory;
import io.redlink.more.studymanager.component.observation.PolarVerityObservationFactory;
import io.redlink.more.studymanager.component.observation.QuestionObservationFactory;
import io.redlink.more.studymanager.component.observation.garmin.activity.GarminActivityObservationFactory;
import io.redlink.more.studymanager.component.observation.garmin.bloodpressure.GarminBloodPressureObservationFactory;
import io.redlink.more.studymanager.component.observation.garmin.heartrate.GarminHeartRateObservationFactory;
import io.redlink.more.studymanager.component.observation.garmin.sleep.GarminSleepObservationFactory;
import io.redlink.more.studymanager.component.observation.garmin.steps.daily.GarminDailyStepsObservationFactory;
import io.redlink.more.studymanager.component.observation.lime.LimeSurveyObservationFactory;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.validation.ConfigurationValidationReport;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.repository.ObservationRepository;
import io.redlink.more.studymanager.sdk.MoreSDK;
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
    Map<String, ObservationFactory> observationFactories;

    @Mock
    StudyStateService studyStateService;

    @Mock
    ObservationRepository repository;

    @Mock
    MoreSDK sdk;

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

        Observation garminActivity = new Observation().setType("garmin-activity-observation");
        Observation garminBloodPressure = new Observation().setType("garmin-blood-pressure-observation");
        Observation garminHeartRate = new Observation().setType("garmin-heart-rate-observation");
        Observation garminSleep = new Observation().setType("garmin-sleep-observation");
        Observation garminSteps = new Observation().setType("garmin-steps-observation");

        AccMobileObservationFactory accFactory = new AccMobileObservationFactory();
        PolarVerityObservationFactory polFactory = new PolarVerityObservationFactory();
        GpsMobileObservationFactory gpsFactory = new GpsMobileObservationFactory();
        LimeSurveyObservationFactory limFactory = new LimeSurveyObservationFactory();
        QuestionObservationFactory qstFactory = new QuestionObservationFactory();
        ExternalObservationFactory extFactory = new ExternalObservationFactory();

        GarminActivityObservationFactory garminActivityFactory = new GarminActivityObservationFactory();
        GarminBloodPressureObservationFactory garminBloodPressureFactory = new GarminBloodPressureObservationFactory();
        GarminHeartRateObservationFactory garminHeartRateFactory = new GarminHeartRateObservationFactory();
        GarminSleepObservationFactory garminSleepFactory = new GarminSleepObservationFactory();
        GarminDailyStepsObservationFactory garminStepsFactory = new GarminDailyStepsObservationFactory();

        assertThat(accFactory.getHidden()).isTrue();
        assertThat(polFactory.getHidden()).isTrue();
        assertThat(gpsFactory.getHidden()).isTrue();
        assertThat(limFactory.getHidden()).isTrue();
        assertThat(qstFactory.getHidden()).isTrue();

        assertThat(extFactory.getVisibility().isChangeable()).isFalse();

        assertThat(garminActivityFactory.getVisibility().isHiddenByDefault()).isFalse();
        assertThat(garminBloodPressureFactory.getVisibility().isHiddenByDefault()).isFalse();
        assertThat(garminHeartRateFactory.getVisibility().isHiddenByDefault()).isFalse();
        assertThat(garminSleepFactory.getVisibility().isHiddenByDefault()).isFalse();
        assertThat(garminStepsFactory.getVisibility().isHiddenByDefault()).isFalse();
    }
    
    @Test
    void testGetObservationFactory_optional() {
        Observation obs = new Observation().setType("x");
        ObservationFactory factory = org.mockito.Mockito.mock(ObservationFactory.class);
        org.mockito.Mockito.when(observationFactories.get("x")).thenReturn(factory);

        java.util.Optional<ObservationFactory> present = observationService.getObservationFactory(obs);
        org.assertj.core.api.Assertions.assertThat(present).containsSame(factory);

        org.mockito.Mockito.when(observationFactories.get("x")).thenReturn(null);
        java.util.Optional<ObservationFactory> empty = observationService.getObservationFactory(obs);
        org.assertj.core.api.Assertions.assertThat(empty).isEmpty();
    }

    @Test
    void testGetParticipantObservationProperties_delegatesToRepository() {
        java.util.List<io.redlink.more.studymanager.model.ParticipantWithObservationProperties> expected = java.util.List.of();
        org.mockito.Mockito.when(repository.getParticipantObservationProperties(5L)).thenReturn(expected);

        java.util.List<io.redlink.more.studymanager.model.ParticipantWithObservationProperties> result = observationService.getParticipantObservationProperties(5L);

        org.assertj.core.api.Assertions.assertThat(result).isSameAs(expected);
        org.mockito.Mockito.verify(repository).getParticipantObservationProperties(5L);
    }

    @Test
    void testListDataViews_usesFactoryAndSdk() {
        Observation obs = new Observation().setStudyId(10L).setStudyGroupId(20).setObservationId(30).setType("t");
        org.mockito.Mockito.when(repository.getById(10L, 30)).thenReturn(obs);

        io.redlink.more.studymanager.core.ui.DataViewInfo[] infos = new io.redlink.more.studymanager.core.ui.DataViewInfo[]{
                org.mockito.Mockito.mock(io.redlink.more.studymanager.core.ui.DataViewInfo.class)
        };

        io.redlink.more.studymanager.core.component.Observation component = org.mockito.Mockito.mock(io.redlink.more.studymanager.core.component.Observation.class);
        org.mockito.Mockito.when(component.listViews()).thenReturn(infos);

        ObservationFactory factory = org.mockito.Mockito.mock(ObservationFactory.class);
        org.mockito.Mockito.when(observationFactories.get("t")).thenReturn(factory);
        org.mockito.Mockito.when(factory.create(org.mockito.Mockito.any(), org.mockito.Mockito.any())).thenReturn(component);


        io.redlink.more.studymanager.core.ui.DataViewInfo[] result = observationService.listDataViews(10L, 30);
        org.assertj.core.api.Assertions.assertThat(result).isSameAs(infos);

        org.mockito.Mockito.verify(observationFactories).get("t");
        org.mockito.Mockito.verify(factory).create(org.mockito.Mockito.any(), org.mockito.Mockito.any());
        org.mockito.Mockito.verify(component).listViews();
    }

    @Test
    void testQueryData_usesFactoryAndSdk() {
        Observation obs = new Observation().setStudyId(10L).setStudyGroupId(21).setObservationId(31).setType("t2");
        org.mockito.Mockito.when(repository.getById(10L, 31)).thenReturn(obs);

        io.redlink.more.studymanager.core.ui.DataView dataView = org.mockito.Mockito.mock(io.redlink.more.studymanager.core.ui.DataView.class);
        io.redlink.more.studymanager.core.component.Observation component = org.mockito.Mockito.mock(io.redlink.more.studymanager.core.component.Observation.class);
        org.mockito.Mockito.when(component.getView(org.mockito.Mockito.eq("v2"), org.mockito.Mockito.eq(21), org.mockito.Mockito.eq(1), org.mockito.Mockito.any())).thenReturn(dataView);

        ObservationFactory factory = org.mockito.Mockito.mock(ObservationFactory.class);
        org.mockito.Mockito.when(observationFactories.get("t2")).thenReturn(factory);
        org.mockito.Mockito.when(factory.create(org.mockito.Mockito.any(), org.mockito.Mockito.any())).thenReturn(component);


        io.redlink.more.studymanager.core.ui.DataView result = observationService.queryData(10L, 31, "v2", 21, 1, null);
        org.assertj.core.api.Assertions.assertThat(result).isSameAs(dataView);

        org.mockito.Mockito.verify(observationFactories).get("t2");
        org.mockito.Mockito.verify(factory).create(org.mockito.Mockito.any(), org.mockito.Mockito.any());
        org.mockito.Mockito.verify(component).getView(org.mockito.Mockito.eq("v2"), org.mockito.Mockito.eq(21), org.mockito.Mockito.eq(1), org.mockito.Mockito.any());
    }
}
