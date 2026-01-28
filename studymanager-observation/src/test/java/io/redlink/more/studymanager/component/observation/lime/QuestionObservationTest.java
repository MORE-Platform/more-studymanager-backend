package io.redlink.more.studymanager.component.observation.lime;

import io.redlink.more.studymanager.component.observation.QuestionObservation;
import io.redlink.more.studymanager.component.observation.QuestionObservationFactory;
import io.redlink.more.studymanager.core.datavalidity.*;
import io.redlink.more.studymanager.core.measurement.Measurement;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.Mockito.mock;

public class QuestionObservationTest {


    @Test
    public void testValidation(){
        MoreObservationSDK sdk = mock(MoreObservationSDK.class);
        ObservationProperties properties = mock(ObservationProperties.class);

        Instant start = Instant.now().truncatedTo(ChronoUnit.HOURS).minus(1, ChronoUnit.HOURS);
        Instant end = start.plus(1, ChronoUnit.HOURS);
        Instant stored = start.plus(45, ChronoUnit.MINUTES);

        QuestionObservation questionObservation = new QuestionObservation(sdk, properties);

        var answerSummary = new MeasurementSummary(
                new Measurement(QuestionObservationFactory.FIELD_ANSWER, Measurement.Type.STRING));
        answerSummary.setStringResult(new StringMeasurementSummary(List.of(new StringFieldValue("Antwort 1", 1))));
        ObservationDataSummary validSummary = new ObservationDataSummary(
                1,
                new DateMeasurementSummary(stored,stored,0L),
                List.of(answerSummary)
        );
        var result = questionObservation.validateData(start, end, validSummary);
        Assertions.assertFalse(result.invalid());
        Assertions.assertEquals(ObservationDataState.COMPLETE, result.state());

        ObservationDataSummary invalidMultipleAnswersSummary = new ObservationDataSummary(
                2,
                new DateMeasurementSummary(stored,stored,0L),
                List.of(answerSummary)
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

        var nullAnswerSummary = new MeasurementSummary(
                new Measurement(QuestionObservationFactory.FIELD_ANSWER, Measurement.Type.STRING));
        nullAnswerSummary.setStringResult(new StringMeasurementSummary(List.of(new StringFieldValue(null, 1))));
        ObservationDataSummary invalidAnswerSummary = new ObservationDataSummary(
                1,
                new DateMeasurementSummary(stored,stored,0L),
                List.of(nullAnswerSummary)
        );
        result = questionObservation.validateData(start, end, invalidAnswerSummary);
        Assertions.assertTrue(result.invalid());
        Assertions.assertEquals(ObservationDataState.MISSING, result.state());


    }
}
