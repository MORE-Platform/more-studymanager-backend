/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.component.trigger.datacheck;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.redlink.more.studymanager.core.properties.TriggerProperties;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ScheduledDatacheckTriggerPropertiesTest {

    @Test
    void getElasticQueryString() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TriggerProperties tp = mapper.readValue(
                Resources.getResource("ScheduledDatacheckTriggerProperties.json"),
                TriggerProperties.class
        );
        ScheduledDatacheckTriggerProperties properties = new ScheduledDatacheckTriggerProperties(tp);

        assertTrue(properties.getElasticQueryString().get().contains("observation_id.keyword:17 AND data_y:23"));
    }
}
