package io.redlink.more.studymanager.component.observation.lime;

import io.redlink.more.studymanager.component.observation.QuestionObservation;
import io.redlink.more.studymanager.component.observation.QuestionObservationFactory;
import io.redlink.more.studymanager.component.observation.lime.model.ParticipantData;
import io.redlink.more.studymanager.core.datavalidity.DateMeasurementSummary;
import io.redlink.more.studymanager.core.datavalidity.FieldValue;
import io.redlink.more.studymanager.core.datavalidity.MeasurementSummary;
import io.redlink.more.studymanager.core.datavalidity.NumericMeasurementSummary;
import io.redlink.more.studymanager.core.datavalidity.ObservationDataState;
import io.redlink.more.studymanager.core.datavalidity.ObservationDataSummary;
import io.redlink.more.studymanager.core.datavalidity.StringMeasurementSummary;
import io.redlink.more.studymanager.core.measurement.Measurement;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;
import io.redlink.more.studymanager.core.sdk.MorePlatformSDK;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LimeSurveyObservationTest {
    @Test
    public void testLimeSurveyIdValidation() {
        MoreObservationSDK sdk = mock(MoreObservationSDK.class);
        ObservationProperties properties = mock(ObservationProperties.class);

        when(sdk.getValue(anyString(), any()))
                .thenReturn(Optional.empty(), Optional.of("equals"), Optional.of("other"));
        when(properties.getString(anyString())).thenReturn("valid", "equals", "different");

        LimeSurveyObservation o = new LimeSurveyObservation(sdk, properties, null);
        Assertions.assertEquals("valid", o.checkAndGetSurveyId().get());
        Assertions.assertEquals("equals", o.checkAndGetSurveyId().get());
    }

    @Test
    public void testActivation() {
        MoreObservationSDK sdk = mock(MoreObservationSDK.class);
        LimeSurveyRequestService limeSurveyRequestService = mock(LimeSurveyRequestService.class);
        ObservationProperties properties = new ObservationProperties();

        long studyId = 42L;
        int observationId = 123;

        String limeSurveyId = "lime-id-1";
        Set<Integer> studyParticipantIds = Set.of(1, 2, 3, 4, 5, 6);
        properties.put("limeSurveyId", limeSurveyId);

        LimeSurveyObservation questionObservation = new LimeSurveyObservation(sdk, properties, limeSurveyRequestService);

        Mockito.when(sdk.getStudyId()).thenReturn(studyId);
        Mockito.when(sdk.getObservationId()).thenReturn(observationId);
        Mockito.when(sdk.participantIds(eq(MorePlatformSDK.ParticipantFilter.ALL)))
                .thenReturn(studyParticipantIds);
        Mockito.when(sdk.getValue(eq("limeSurveyId"), eq(String.class)))
                .thenReturn(null);
        Mockito.when(limeSurveyRequestService.listParticipants(eq(limeSurveyId), eq(0), eq(1000)))
                .thenReturn(List.of(
                        new ParticipantData( //old format
                                new ParticipantData.ParticipantInfo("1", "1", null),
                                "token-1",
                                1),
                        new ParticipantData( //old format
                                new ParticipantData.ParticipantInfo("2", "2", null),
                                "token-2",
                                2),
                        new ParticipantData( //new format other study
                                new ParticipantData.ParticipantInfo("study_4-observation_4-participant_3", "more", null),
                                "token-other-study",
                                -1),
                        new ParticipantData( //new format same study other observation
                                new ParticipantData.ParticipantInfo("study_42-observation_4-participant_4", "more", null),
                                "token-other-observation",
                                -2),
                        new ParticipantData( //participant 3 of the actual study
                                new ParticipantData.ParticipantInfo("study_42-observation_123-participant_3", "more", null),
                                "token-3",
                                3),
                        new ParticipantData( //participant 4
                                new ParticipantData.ParticipantInfo("study_42-observation_123-participant_4", "more", null),
                                "token-4",
                                4),
                        new ParticipantData( //not existing participant to validate deletion
                                new ParticipantData.ParticipantInfo("study_42-observation_123-participant_99", "more", null),
                                "token-99",
                                99)
                ));
        Mockito.doNothing().when(limeSurveyRequestService).deleteParticipants(eq(limeSurveyId), eq(Set.of(99)));

        Mockito.when(limeSurveyRequestService.activateParticipants(ArgumentMatchers.anySet(),eq(limeSurveyId)))
                .thenAnswer( input -> {
                    int initialValue = 5;
                    AtomicInteger i = new AtomicInteger(initialValue);
                    var pd = ((Set<ParticipantData.ParticipantInfo>)input.getArguments()[0])
                            .stream()
                            .map(pi -> {
                                Assertions.assertNotNull(pi);
                                Assertions.assertEquals("more", pi.lastname());
                                Assertions.assertTrue(pi.firstname().startsWith("study_42-observation_123-participant_"));
                                Integer pid = Integer.parseInt(pi.firstname().substring(pi.firstname().lastIndexOf('_')+1));
                                return new AbstractMap.SimpleEntry<>(pid, pi);
                            })
                            .map(entry -> new ParticipantData(
                                entry.getValue(),
                                "token-" + entry.getKey(),
                                entry.getKey()))
                            .toList();
                    Assertions.assertEquals(2, pd.size());
                    return pd;
                });

        Mockito.doNothing().when(limeSurveyRequestService).setSurveyEndUrl(eq(limeSurveyId), eq(studyId), eq(observationId));
        Mockito.doNothing().when(limeSurveyRequestService).activateSurvey(eq(limeSurveyId));

        Mockito.doNothing().when(sdk).mergePropertiesForParticipant(any(Integer.class),any(ObservationProperties.class));

        Mockito.when(limeSurveyRequestService.getBaseUrl()).thenReturn("http://example.com/lime-base-url");

        questionObservation.activate();

        Mockito.verify(limeSurveyRequestService, Mockito.times(1)).deleteParticipants(eq(limeSurveyId), eq(Set.of(99)));
        Mockito.verify(limeSurveyRequestService, Mockito.times(1)).setSurveyEndUrl(eq(limeSurveyId), eq(studyId), eq(observationId));
        Mockito.verify(limeSurveyRequestService, Mockito.times(1)).activateSurvey(eq(limeSurveyId));
        //called once for every participant
        Mockito.verify(sdk, Mockito.times(studyParticipantIds.size())).mergePropertiesForParticipant(any(Integer.class),any(ObservationProperties.class));
    }


    @Test
    public void testValidation() {
        MoreObservationSDK sdk = mock(MoreObservationSDK.class);
        ObservationProperties properties = mock(ObservationProperties.class);

        Instant start = Instant.now().truncatedTo(ChronoUnit.HOURS).minus(1, ChronoUnit.HOURS);
        Instant end = start.plus(1, ChronoUnit.HOURS);
        Instant stored = start.plus(45, ChronoUnit.MINUTES);

        LimeSurveyObservation questionObservation = new LimeSurveyObservation(sdk, properties, null);

        var seedSummary = new MeasurementSummary(
                new Measurement(LimeSurveyObservationFactory.MEASUREMENT_SEED, Measurement.Type.STRING));
        seedSummary.setStringResult(new StringMeasurementSummary(List.of(new FieldValue<>("seed_12345", 1))));
        var idMs = new MeasurementSummary(
                new Measurement(LimeSurveyObservationFactory.MEASUREMENT_ID, Measurement.Type.INTEGER));
        idMs.setNumericResult(new NumericMeasurementSummary(1.0,1.0, 1.0, 1.0, 0));
        ObservationDataSummary validSummary = new ObservationDataSummary(
                1,
                new DateMeasurementSummary(stored, stored, 0L),
                List.of(idMs, seedSummary)
        );
        var result = questionObservation.validateData(start, end, validSummary);
        Assertions.assertFalse(result.invalid());
        Assertions.assertEquals(ObservationDataState.COMPLETE, result.state());

        ObservationDataSummary invalidMultipleAnswersSummary = new ObservationDataSummary(
                2,
                new DateMeasurementSummary(stored, stored, 0L),
                List.of(idMs, seedSummary)
        );
        result = questionObservation.validateData(start, end, invalidMultipleAnswersSummary);
        Assertions.assertTrue(result.invalid());
        Assertions.assertEquals(ObservationDataState.COMPLETE, result.state());

        ObservationDataSummary noAnswersSummary = new ObservationDataSummary(
                0,
                null,
                null
        );
        result = questionObservation.validateData(start, end, noAnswersSummary);
        Assertions.assertFalse(result.invalid());
        Assertions.assertEquals(ObservationDataState.MISSING, result.state());

        var nullSeedMs = new MeasurementSummary(
                new Measurement(LimeSurveyObservationFactory.MEASUREMENT_SEED, Measurement.Type.STRING));
        nullSeedMs.setStringResult(new StringMeasurementSummary(List.of(new FieldValue<String>(null, 1))));
        ObservationDataSummary nullSeedSummary = new ObservationDataSummary(
                1,
                new DateMeasurementSummary(stored, stored, 0L),
                List.of(idMs, nullSeedMs)
        );
        result = questionObservation.validateData(start, end, nullSeedSummary);
        Assertions.assertTrue(result.invalid());
        Assertions.assertEquals(ObservationDataState.INCOMPLETE, result.state());

        var missingIdMs = new MeasurementSummary(
                new Measurement(LimeSurveyObservationFactory.MEASUREMENT_ID, Measurement.Type.INTEGER));
        missingIdMs.setNumericResult(new NumericMeasurementSummary(0.0f, 0.0f, 0.0f, 0.0f, 1));
        ObservationDataSummary missingIdSummary = new ObservationDataSummary(
                1,
                new DateMeasurementSummary(stored, stored, 0L),
                List.of(missingIdMs, seedSummary)
        );
        result = questionObservation.validateData(start, end, missingIdSummary);
        Assertions.assertTrue(result.invalid());
        Assertions.assertEquals(ObservationDataState.INCOMPLETE, result.state());


    }}
