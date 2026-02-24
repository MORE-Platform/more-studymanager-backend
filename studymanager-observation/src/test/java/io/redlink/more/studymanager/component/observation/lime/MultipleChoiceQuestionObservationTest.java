package io.redlink.more.studymanager.component.observation.lime;

import io.redlink.more.studymanager.component.observation.MultipleChoiceQuestionObservation;
import io.redlink.more.studymanager.component.observation.MultipleChoiceQuestionObservationFactory;
import io.redlink.more.studymanager.core.datavalidity.ArrayMeasurementSummary;
import io.redlink.more.studymanager.core.datavalidity.DateMeasurementSummary;
import io.redlink.more.studymanager.core.datavalidity.FieldValue;
import io.redlink.more.studymanager.core.datavalidity.MeasurementSummary;
import io.redlink.more.studymanager.core.datavalidity.ObservationDataState;
import io.redlink.more.studymanager.core.datavalidity.ObservationDataSummary;
import io.redlink.more.studymanager.core.measurement.Measurement;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.Mockito.mock;

public class MultipleChoiceQuestionObservationTest {

    @Test
    public void testValidation() {
        MoreObservationSDK sdk = mock(MoreObservationSDK.class);
        ObservationProperties properties = mock(ObservationProperties.class);

        Instant start = Instant.now().truncatedTo(ChronoUnit.HOURS).minus(1, ChronoUnit.HOURS);
        Instant end = start.plus(1, ChronoUnit.HOURS);
        Instant stored = start.plus(45, ChronoUnit.MINUTES);

        MultipleChoiceQuestionObservation<?> observation = new MultipleChoiceQuestionObservation<>(sdk, properties);

        var answerSummary = new MeasurementSummary(
                new Measurement(MultipleChoiceQuestionObservationFactory.FIELD_ANSWERS, Measurement.Type.STRING_ARRAY));
        answerSummary.setArrayResult(new ArrayMeasurementSummary<>(new FieldValue<>(List.of("Antwort1", "Antwort 2"), 2)));
        ObservationDataSummary validSummary = new ObservationDataSummary(
                1,
                new DateMeasurementSummary(stored, stored, 0L),
                List.of(answerSummary)
        );
        var result = observation.validateData(start, end, validSummary);
        Assertions.assertFalse(result.invalid());
        Assertions.assertEquals(ObservationDataState.COMPLETE, result.state());

        ObservationDataSummary invalidMultipleAnswersSummary = new ObservationDataSummary(
                2,
                new DateMeasurementSummary(stored, stored, 0L),
                List.of(answerSummary)
        );
        result = observation.validateData(start, end, invalidMultipleAnswersSummary);
        Assertions.assertTrue(result.invalid());
        Assertions.assertEquals(ObservationDataState.COMPLETE, result.state());

        ObservationDataSummary noAnswersSummary = new ObservationDataSummary(
                0,
                null,
                null
        );
        result = observation.validateData(start, end, noAnswersSummary);
        Assertions.assertFalse(result.invalid());
        Assertions.assertEquals(ObservationDataState.MISSING, result.state());

        var nullAnswerSummary = new MeasurementSummary(
                new Measurement(MultipleChoiceQuestionObservationFactory.FIELD_ANSWERS, Measurement.Type.STRING_ARRAY));
        nullAnswerSummary.setArrayResult(new ArrayMeasurementSummary<String>(new FieldValue<>(null, 1)));
        ObservationDataSummary invalidAnswerSummary = new ObservationDataSummary(
                1,
                new DateMeasurementSummary(stored, stored, 0L),
                List.of(nullAnswerSummary)
        );
        result = observation.validateData(start, end, invalidAnswerSummary);
        Assertions.assertTrue(result.invalid());
        Assertions.assertEquals(ObservationDataState.MISSING, result.state());
    }
}
