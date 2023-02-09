/*
 * Copyright (c) 2023 Redlink GmbH.
 */
package io.redlink.more.studymanager.model.transformer;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Function;

/**
 * Common basic transformers.
 */
public final class Transformers {

    private static final ZoneId HOME = ZoneId.of("Europe/Vienna");

    private Transformers() {
    }

    /**
     * @deprecated Use {@link LocalDate} or {@link Instant} (as appropriate) on the business-logic/model layer.
     */
    @Deprecated(forRemoval = true)
    public static LocalDate toLocalDate(Date date) {
        return transform(date, d ->
                d.toInstant()
                        .atZone(HOME)
                        .toLocalDate()
        );
    }

    /**
     * @deprecated Use {@link LocalDate} on the business-logic/model layer.
     */
    @Deprecated(forRemoval = true)
    public static LocalDate toLocalDate(java.sql.Date date) {
        return transform(date, java.sql.Date::toLocalDate);
    }

    /**
     * This is a placeholder to help with the model-migration to {@link LocalDate}.
     */
    public static LocalDate toLocalDate(final LocalDate localDate) {
        return localDate;
    }

    /**
     * @deprecated Use {@link Instant} or {@link LocalDate} (as appropriate) on the business-logic/model layer.
     */
    @Deprecated(forRemoval = true)
    public static OffsetDateTime toOffsetDateTime(Date date) {
        return transform(date, d ->
                d.toInstant()
                        .atZone(HOME)
                        .toOffsetDateTime()
        );
    }

    /**
     * @deprecated Use {@link LocalDate} on the business-logic/model layer.
     */
    @Deprecated(forRemoval = true)
    public static java.sql.Date toSqlDate(LocalDate localDate) {
        return transform(localDate, java.sql.Date::valueOf);
    }

    public static OffsetDateTime toOffsetDateTime(Instant instant) {
        return transform(instant, i -> i.atZone(HOME).toOffsetDateTime());
    }

    /**
     * @deprecated Use {@link Instant} on the business-logic/model layer.
     */
    @Deprecated(forRemoval = true)
    public static OffsetDateTime toOffsetDateTime(Timestamp timestamp) {
        return transform(timestamp, t -> toOffsetDateTime(t.toInstant()));
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
