package io.redlink.more.studymanager.component.observation.lime;

import io.redlink.more.studymanager.component.observation.QuestionObservation;
import io.redlink.more.studymanager.component.observation.QuestionObservationFactory;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
