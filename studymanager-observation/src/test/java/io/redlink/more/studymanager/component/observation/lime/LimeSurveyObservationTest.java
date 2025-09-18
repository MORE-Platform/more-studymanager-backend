package io.redlink.more.studymanager.component.observation.lime;

import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Assertions.assertEquals("valid", o.checkAndGetSurveyId());
        Assertions.assertEquals("equals", o.checkAndGetSurveyId());
        boolean expectedError = false;
        try {
            o.checkAndGetSurveyId();
        } catch (RuntimeException e) {
            expectedError = true;
        }

        Assertions.assertTrue(expectedError);
    }
}
