/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model.transformer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.more.studymanager.api.v1.model.InterventionDTO;
import io.redlink.more.studymanager.api.v1.model.StudyImportExportDTO;
import io.redlink.more.studymanager.model.StudyImportExport;
import io.redlink.more.studymanager.utils.MapperUtils;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
                .setInterventions(dto.getInterventions().stream().map(
                        InterventionTransformer::fromInterventionDTO_V1
                ).toList())
                .setTriggers(
                        dto.getInterventions().stream().collect(Collectors.toMap(
                                InterventionDTO::getInterventionId, interventionDTO ->
                                        TriggerTransformer.fromTriggerDTO_V1(interventionDTO.getTrigger()))))
                .setActions(
                        dto.getInterventions().stream().collect(Collectors.toMap(
                                InterventionDTO::getInterventionId, interventionDTO ->
                                        interventionDTO.getActions().stream().map(ActionTransformer::fromActionDTO_V1).toList()))
                );
    }

    public static StudyImportExportDTO toStudyImportExportDTO_V1(StudyImportExport studyImportExport) {
        return new StudyImportExportDTO()
                .study(StudyTransformer.toStudyDTO_V1(studyImportExport.getStudy()))
                .studyGroups(studyImportExport.getStudyGroups().stream().map(
                        StudyGroupTransformer::toStudyGroupDTO_V1).toList())
                .observations(studyImportExport.getObservations().stream().map(
                        ObservationTransformer::toObservationDTO_V1).toList())
                .interventions(studyImportExport.getInterventions().stream().map( intervention ->
                    InterventionTransformer.toInterventionDTO_V1(intervention)
                            .trigger(
                                    TriggerTransformer.toTriggerDTO_V1(
                                            studyImportExport.getTriggers().get(intervention.getInterventionId())
                                    )
                            )
                            .actions(studyImportExport.getActions().get(intervention.getInterventionId())
                                    .stream().map(ActionTransformer::toActionDTO_V1).toList())).toList());
    }
}
