package io.redlink.more.studymanager.component.trigger.relative;

import io.redlink.more.studymanager.core.io.SimpleParticipant;
import io.redlink.more.studymanager.core.io.TimeRange;
import io.redlink.more.studymanager.core.properties.TriggerProperties;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.core.sdk.schedule.Schedule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

class RelativeTimeTriggerTest {

    private static final ZoneId HOME = ZoneId.of("Europe/Vienna");

    @Test
    void testParticipantFilter() {
        TriggerProperties properties = new TriggerProperties(Map.of(
                "day", 2, "hour", 8
        ));
        RelativeTimeTrigger trigger = new RelativeTimeTrigger(noMilestoneSdk(), properties);
        Instant now1 = Instant.parse("2024-01-23T10:00:01.00Z");
        Instant now2 = Instant.parse("2024-01-23T07:00:01.00Z");
        SimpleParticipant p1 = new SimpleParticipant(1, Instant.parse("2024-01-21T11:13:01.00Z"));
        SimpleParticipant p2 = new SimpleParticipant(1, Instant.parse("2024-01-22T10:14:01.00Z"));

        Assertions.assertFalse(trigger.matchesDayAndHour(p1, now1));
        Assertions.assertFalse(trigger.matchesDayAndHour(p2, now1));
        Assertions.assertFalse(trigger.matchesDayAndHour(p1, now2));
        Assertions.assertTrue(trigger.matchesDayAndHour(p2, now2));
    }

    @Test
    void testMilestoneAnchorSkipsOneBasedCorrection() {
        TriggerProperties properties = new TriggerProperties(Map.of(
                "day", 2, "hour", 8
        ));

        // milestone reached at 2024-01-21T00:00 HOME time; queried from the SDK, not stored on the participant
        Instant milestoneDateTime = Instant.parse("2024-01-20T23:00:00.00Z");
        RelativeTimeTrigger trigger = new RelativeTimeTrigger(
                sdkWithMilestone(3, milestoneDateTime), properties);
        SimpleParticipant milestoneParticipant = new SimpleParticipant(3, Instant.parse("2020-01-01T00:00:00.00Z"));

        // milestone anchor: day 2 means exactly two days after the milestone, no 1-based correction
        Instant twoDaysAfterAt8am = Instant.parse("2024-01-23T07:00:01.00Z");
        Assertions.assertTrue(trigger.matchesDayAndHour(milestoneParticipant, twoDaysAfterAt8am));

        // one day after (which would match day=2 under the signup's 1-based convention) must NOT match
        Instant oneDayAfterAt8am = Instant.parse("2024-01-22T07:00:01.00Z");
        Assertions.assertFalse(trigger.matchesDayAndHour(milestoneParticipant, oneDayAfterAt8am));

        // a participant with no milestone reached yet still uses the signup-based, 1-based day arithmetic:
        // day=2 matches one calendar day after signup (not two, as it would for a milestone anchor)
        RelativeTimeTrigger signupOnlyTrigger = new RelativeTimeTrigger(noMilestoneSdk(), properties);
        SimpleParticipant signupOnlyParticipant = new SimpleParticipant(4, Instant.parse("2024-01-21T10:14:01.00Z"));
        Assertions.assertTrue(signupOnlyTrigger.matchesDayAndHour(signupOnlyParticipant, oneDayAfterAt8am));
    }

    private static MoreTriggerSDK noMilestoneSdk() {
        return sdkResolving(participantId -> Optional.empty());
    }

    private static MoreTriggerSDK sdkWithMilestone(Integer participantId, Instant dateTime) {
        return sdkResolving(id -> participantId.equals(id) ? Optional.of(dateTime) : Optional.empty());
    }

    private static MoreTriggerSDK sdkResolving(Function<Integer, Optional<Instant>> milestoneDateTimeResolver) {
        return new MoreTriggerSDK() {
            @Override
            public Optional<Instant> getMilestoneDateTime(Integer participantId) {
                return milestoneDateTimeResolver.apply(participantId);
            }

            @Override
            public String addSchedule(Schedule schedule) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void removeSchedule(String id) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Set<Integer> participantIdsMatchingQuery(String query, TimeRange timeRange) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String addWebhook() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void removeWebhook() {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T extends Serializable> void setValue(String name, T value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T extends Serializable> Optional<T> getValue(String name, Class<T> tClass) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void removeValue(String name) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Set<Integer> participantIds(ParticipantFilter filter) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Set<SimpleParticipant> participants(ParticipantFilter filter) {
                throw new UnsupportedOperationException();
            }

            @Override
            public long getStudyId() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Integer getStudyGroupId() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
