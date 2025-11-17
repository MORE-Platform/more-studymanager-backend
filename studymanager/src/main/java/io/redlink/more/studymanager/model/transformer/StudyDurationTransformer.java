package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.DurationDTO;
import io.redlink.more.studymanager.api.v1.model.StudyDurationDTO;
import io.redlink.more.studymanager.model.scheduler.Duration;

public class StudyDurationTransformer {
    public static Duration.Unit fromDurationDTOUnit(DurationDTO.UnitEnum unit) {
        if(unit == null) return null;
        return switch (unit) {
            case MINUTE -> Duration.Unit.MINUTE;
            case HOUR -> Duration.Unit.HOUR;
            case DAY -> Duration.Unit.DAY;
            default -> throw new IllegalArgumentException("Unexpected value '" + unit + "'");
        };
    }

    public static DurationDTO.UnitEnum toDurationDTOUnit(Duration.Unit unit) {
        if(unit == null) return null;
        return switch (unit) {
            case MINUTE -> DurationDTO.UnitEnum.MINUTE;
            case HOUR -> DurationDTO.UnitEnum.HOUR;
            case DAY -> DurationDTO.UnitEnum.DAY;
            default -> throw new IllegalArgumentException("Unexpected value '" + unit + "'");
        };
    }

    public static Duration.Unit fromStudyDurationDTOUnit(StudyDurationDTO.UnitEnum unit) {
        if(unit == null) return null;
        return switch (unit) {
            case MINUTE -> Duration.Unit.MINUTE;
            case HOUR -> Duration.Unit.HOUR;
            case DAY -> Duration.Unit.DAY;
            default -> throw new IllegalArgumentException("Unexpected value '" + unit + "'");
        };
    }

    public static StudyDurationDTO.UnitEnum toStudyDurationDTOUnit(Duration.Unit unit) {
        if(unit == null) return null;
        return switch (unit) {
            case MINUTE -> StudyDurationDTO.UnitEnum.MINUTE;
            case HOUR -> StudyDurationDTO.UnitEnum.HOUR;
            case DAY -> StudyDurationDTO.UnitEnum.DAY;
            default -> throw new IllegalArgumentException("Unexpected value '" + unit + "'");
        };
    }

    public static StudyDurationDTO toStudyDurationDTO(Duration duration) {
        if (duration != null)
            return new StudyDurationDTO()
                    .value(duration.getValue())
                    .unit(StudyDurationTransformer.toStudyDurationDTOUnit(duration.getUnit()));
        else return null;
    }

    public static Duration fromStudyDurationDTO(StudyDurationDTO dto) {
        if (dto != null)
            return new Duration()
                    .setValue(dto.getValue())
                    .setUnit(StudyDurationTransformer.fromStudyDurationDTOUnit(dto.getUnit()));
        else return null;
    }

    public static DurationDTO toDurationDTO(Duration duration) {
        if (duration != null)
            return new DurationDTO()
                    .value(duration.getValue())
                    .unit(StudyDurationTransformer.toDurationDTOUnit(duration.getUnit()));
        else return null;
    }

    public static Duration fromDurationDTO(DurationDTO dto) {
        if (dto != null)
            return new Duration()
                    .setValue(dto.getValue())
                    .setUnit(StudyDurationTransformer.fromDurationDTOUnit(dto.getUnit()));
        else return null;
    }
}
