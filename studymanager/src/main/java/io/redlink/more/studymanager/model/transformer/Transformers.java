/*
 * Copyright (c) 2023 Redlink GmbH.
 */
package io.redlink.more.studymanager.model.transformer;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.function.Function;

/**
 * Common basic transformers.
 */
public final class Transformers {

    private static final ZoneId HOME = ZoneId.of("Europe/Vienna");

    private Transformers() {
    }

    public static OffsetDateTime toOffsetDateTime(Instant instant) {
        return transform(instant, i -> i.atZone(HOME).toOffsetDateTime());
    }

    public static Instant toInstant(OffsetDateTime offsetDateTime) {
        return transform(offsetDateTime, OffsetDateTime::toInstant);
    }

    /**
     * Performs a null-safe conversion of {@code t} using the {@code transformer}.
     * @param t the value to transform.
     * @param transformer the transformation-function. the argument passed to this function will never be {@code null}.
     * @return the result of {@code transformer.apply(t)} or {@code null}
     */
    public static <T, R> R transform(final T t, final Function<T, R> transformer) {
        if (t == null) {
            return null;
        }
        return transformer.apply(t);
    }
}
