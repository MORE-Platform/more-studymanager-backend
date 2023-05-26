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

        assertEquals("((observation_id.keyword:17 AND data_y:23) AND (observation_id.keyword:17 AND data_x:>75) AND (observation_id.keyword:17 AND data_x:23)) OR ((observation_id.keyword:15 AND data_long:13.04694) AND (observation_id.keyword:15 AND data_lat:47.80702))",
                properties.getElasticQueryString().get());
    }
}
