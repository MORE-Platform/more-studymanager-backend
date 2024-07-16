/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model.transformer;

import java.time.Instant;
import java.time.ZoneId;
import java.util.function.Function;

/**
 * Common basic transformers.
 */
public final class Transformers {

    private static final ZoneId HOME = ZoneId.of("Europe/Vienna");

    private Transformers() {
    }

    public static Instant toOffsetDateTime(Instant instant) {
        return instant;
    }

    public static Instant toInstant(Instant offsetDateTime) {
        return offsetDateTime;
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
