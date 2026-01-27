package io.redlink.more.studymanager.utils;

import org.apache.commons.lang3.Range;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class RandomSchedulerUtils {
    public static final String OBSERVATION_SCHEDULE_SEED_KEY = "observation_schedule_seed";


    /**
     * Stable 64-bit hash derived from SHA-256. Deterministic across JVMs/platforms.
     * The intent is to generate a seed that changes if the schedule definition changes.
     */
    private static long stableHash64(Object... parts) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            for (Object part : parts) {
                md.update((byte) 0x1F); // field separator
                if (part == null) {
                    md.update((byte) 0x00);
                } else if (part instanceof Long l) {
                    md.update((byte) 0x01);
                    md.update(ByteBuffer.allocate(Long.BYTES).putLong(l).array());
                } else if (part instanceof Integer i) {
                    md.update((byte) 0x02);
                    md.update(ByteBuffer.allocate(Integer.BYTES).putInt(i).array());
                } else if (part instanceof String s) {
                    md.update((byte) 0x03);
                    md.update(s.getBytes(StandardCharsets.UTF_8));
                } else if (part instanceof List<?> list) {
                    md.update((byte) 0x04);
                    // fold in list elements in order
                    for (Object el : list) {
                        md.update((byte) 0x1E);
                        if (el != null) {
                            md.update(el.toString().getBytes(StandardCharsets.UTF_8));
                        }
                    }
                } else {
                    // last resort: string representation
                    md.update((byte) 0x05);
                    md.update(part.toString().getBytes(StandardCharsets.UTF_8));
                }
            }

            byte[] digest = md.digest();
            return ByteBuffer.wrap(digest, 0, 8).getLong();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create deterministic seed", e);
        }
    }

    /**
     * Expand a ScheduleEvent into deterministic randomized intervals (epoch seconds) using a stable seed,
     * resolving relative schedules against the provided {@code start} anchor (like {@link SchedulerUtils}).
     *
     * <p>If {@code end} is provided, the returned ranges are filtered to those that overlap the
     * interval [start,end].</p>
     */
    public static List<Range<Instant>> parseScheduleWithSeed(Long seed, List<Range<Instant>> ranges, Long duration) {
        if (seed == null || ranges == null || ranges.isEmpty() || duration == null || duration <= 0) {
            return Collections.emptyList();
        }
        return IntStream.range(0, ranges.size())
                .mapToObj(i -> {
                    var range = ranges.get(i);
                    var start = range.getMinimum();
                    var end = range.getMaximum();
                    return deterministicRangeForWindow(start, end, duration, seed, i);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Deterministically chooses a start inside [windowStart, windowEnd-duration] and returns [start, start+duration].
     * Returns null if duration does not fit.
     */
    private static Range<Instant> deterministicRangeForWindow(Instant windowStart, Instant windowEnd, long durationSeconds, long seed, long occurrenceIndex) {
        if (windowStart == null || windowEnd == null) {
            return null;
        }
        if (!windowEnd.isAfter(windowStart)) {
            return null;
        }

        long windowStartSec = windowStart.getEpochSecond();
        long windowEndSec = windowEnd.getEpochSecond();

        long latestStartSec = windowEndSec - durationSeconds;
        if (latestStartSec < windowStartSec) {
            return null;
        }

        long available = latestStartSec - windowStartSec;
        long h = stableHash64("Occ", seed, windowStartSec, windowEndSec, durationSeconds, occurrenceIndex);
        long offset = Math.floorMod(h, available + 1);

        long startSec = windowStartSec + offset;
        long endSec = startSec + durationSeconds;
        return Range.of(Instant.ofEpochSecond(startSec), Instant.ofEpochSecond(endSec));
    }

}
