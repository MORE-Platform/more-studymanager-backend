package io.redlink.more.studymanager.component.trigger.relative;

import io.redlink.more.studymanager.core.io.SimpleParticipant;
import io.redlink.more.studymanager.core.properties.TriggerProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;

class RelativeTimeTriggerTest {

    private static final ZoneId HOME = ZoneId.of("Europe/Vienna");
    @Test
    void testParticipantFilter() {
        TriggerProperties properties = new TriggerProperties(Map.of(
                "day", 2, "hour", 8
        ));
        RelativeTimeTrigger trigger = new RelativeTimeTrigger(null, properties);
        Instant now1 = Instant.parse("2024-01-23T10:00:01.00Z");
        Instant now2 = Instant.parse("2024-01-23T07:00:01.00Z");
        SimpleParticipant p1 = new SimpleParticipant(1, Instant.parse("2024-01-21T11:13:01.00Z"));
        SimpleParticipant p2 = new SimpleParticipant(1, Instant.parse("2024-01-22T10:14:01.00Z"));

        Assertions.assertFalse(trigger.matchesDayAndHour(p1, now1));
        Assertions.assertFalse(trigger.matchesDayAndHour(p2, now1));
        Assertions.assertFalse(trigger.matchesDayAndHour(p1, now2));
        Assertions.assertTrue(trigger.matchesDayAndHour(p2, now2));
    }
}
