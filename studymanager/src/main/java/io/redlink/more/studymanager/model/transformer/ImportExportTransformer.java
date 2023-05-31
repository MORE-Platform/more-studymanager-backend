package io.redlink.more.studymanager.model.transformer;

import com.fasterxml.jackson.core.type.TypeReference;
import io.redlink.more.studymanager.api.v1.model.StudyImportExportDTO;
import io.redlink.more.studymanager.model.Action;
import io.redlink.more.studymanager.model.StudyImportExport;
import io.redlink.more.studymanager.model.Trigger;
import io.redlink.more.studymanager.utils.MapperUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportExportTransformer {

    private ImportExportTransformer() {}

    public static StudyImportExport fromStudyImportExportDTO_V1(StudyImportExportDTO dto) {
        return new StudyImportExport()
                .setStudy(StudyTransformer.fromStudyDTO_V1(dto.getStudy()))
                .setStudyGroups(dto.getStudyGroups().stream().map(
                        StudyGroupTransformer::fromStudyGroupDTO_V1
                ).toList())
                .setObservations(dto.getObservations().stream().map(
                        ObservationTransformer::fromObservationDTO_V1
                ).toList())
                .setTriggers(MapperUtils.MAPPER.convertValue(dto.getTriggers(), new TypeReference<>(){}))
                .setActions(MapperUtils.MAPPER.convertValue(dto.getActions(), new TypeReference<>(){}));
    }

    public static StudyImportExportDTO toStudyImportExportDTO_V1(StudyImportExport studyImportExport) {
        return new StudyImportExportDTO()
                .study(StudyTransformer.toStudyDTO_V1(studyImportExport.getStudy()))
                .studyGroups(studyImportExport.getStudyGroups().stream().map(
                        StudyGroupTransformer::toStudyGroupDTO_V1
                ).toList())
                .observations(studyImportExport.getObservations().stream().map(
                        ObservationTransformer::toObservationDTO_V1
                ).toList())
                .triggers(studyImportExport.getTriggers())
                .actions(studyImportExport.getActions());
    }
}
