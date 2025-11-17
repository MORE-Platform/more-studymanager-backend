/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model.scheduler;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

public class Duration {

    private Integer value;

    public java.time.Duration asDuration() {
        return java.time.Duration.of(value, unit.toChronoUnit());
    }

    /**
     * unit of time to offset
     */
    public enum Unit {
        MINUTE("MINUTE", ChronoUnit.MINUTES),

        HOUR("HOUR", ChronoUnit.HOURS),

        DAY("DAY", ChronoUnit.DAYS);

        private String value;

        @JsonIgnore
        private ChronoUnit chronoUnit;

        Unit(String value, ChronoUnit chronoValue) {
            this.value = value;
            this.chronoUnit = chronoValue;
        }


        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static Unit fromValue(String value) {
            for (Unit b : Unit.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }

        public ChronoUnit toChronoUnit() {
            return chronoUnit;
        }
    }

    private Unit unit;

    public Duration() {
    }

    public Integer getValue() {
        return value;
    }

    public Duration setValue(Integer value) {
        this.value = value;
        return this;
    }

    public Unit getUnit() {
        return unit;
    }

    public Duration setUnit(Unit unit) {
        this.unit = unit;
        return this;
    }

    @Override
    public String toString() {
        return "Duration{" +
                "offset=" + value +
                ", unit=" + unit +
                '}';
    }

    public static final Comparator<Duration> DURATION_COMPARATOR =
            Comparator.comparing(d -> java.time.Duration.of(d.value, d.unit.chronoUnit));

}
