package io.redlink.more.studymanager.model.scheduler;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.redlink.more.studymanager.api.v1.model.DurationDTO;
import io.redlink.more.studymanager.api.v1.model.StudyDurationDTO;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Duration {

    private Integer value;

    public Instant getEnd(Instant start) {
        if(start == null) {
            return null;
        }
        return start.plus(value, unit.toChronoUnit());
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

        public static Unit fromDurationDTOUnit(DurationDTO.UnitEnum unit) {
            switch (unit) {
                case MINUTE:
                    return MINUTE;
                case HOUR:
                    return HOUR;
                case DAY:
                    return DAY;
                default:
                    throw new IllegalArgumentException("Unexpected value '" + unit + "'");
            }
        }

        public static DurationDTO.UnitEnum toDurationDTOUnit(Unit unit) {
            switch (unit) {
                case MINUTE:
                    return DurationDTO.UnitEnum.MINUTE;
                case HOUR:
                    return DurationDTO.UnitEnum.HOUR;
                case DAY:
                    return DurationDTO.UnitEnum.DAY;
                default:
                    throw new IllegalArgumentException("Unexpected value '" + unit + "'");
            }
        }

        public static Unit fromStudyDurationDTOUnit(StudyDurationDTO.UnitEnum unit) {
            switch (unit) {
                case MINUTE:
                    return MINUTE;
                case HOUR:
                    return HOUR;
                case DAY:
                    return DAY;
                default:
                    throw new IllegalArgumentException("Unexpected value '" + unit + "'");
            }
        }

        public static StudyDurationDTO.UnitEnum toStudyDurationDTOUnit(Unit unit) {
            switch (unit) {
                case MINUTE:
                    return StudyDurationDTO.UnitEnum.MINUTE;
                case HOUR:
                    return StudyDurationDTO.UnitEnum.HOUR;
                case DAY:
                    return StudyDurationDTO.UnitEnum.DAY;
                default:
                    throw new IllegalArgumentException("Unexpected value '" + unit + "'");
            }
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

    public static StudyDurationDTO toStudyDurationDTO(Duration duration) {
        if (duration != null)
            return new StudyDurationDTO()
                    .value(duration.getValue())
                    .unit(Unit.toStudyDurationDTOUnit(duration.unit));
        else return null;
    }

    public static Duration fromStudyDurationDTO(StudyDurationDTO dto) {
        if (dto != null)
            return new Duration()
                    .setValue(dto.getValue())
                    .setUnit(Unit.fromStudyDurationDTOUnit(dto.getUnit()));
        else return null;
    }

    public static DurationDTO toDurationDTO(Duration duration) {
        if (duration != null)
            return new DurationDTO()
                    .value(duration.getValue())
                    .unit(Unit.toDurationDTOUnit(duration.unit));
        else return null;
    }

    public static Duration fromDurationDTO(DurationDTO dto) {
        if (dto != null)
            return new Duration()
                    .setValue(dto.getValue())
                    .setUnit(Unit.fromDurationDTOUnit(dto.getUnit()));
        else return null;
    }

    @Override
    public String toString() {
        return "Duration{" +
                "offset=" + value +
                ", unit=" + unit +
                '}';
    }
}
