/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.IntegrationInfoDTO;
import io.redlink.more.studymanager.api.v1.model.InterventionDTO;
import io.redlink.more.studymanager.api.v1.model.ParticipantInfoDTO;
import io.redlink.more.studymanager.api.v1.model.StudyImportExportDTO;
import io.redlink.more.studymanager.model.IntegrationInfo;
import io.redlink.more.studymanager.model.StudyImportExport;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ImportExportTransformer {

    private ImportExportTransformer() {}

    public static StudyImportExport fromStudyImportExportDTO_V1(StudyImportExportDTO dto) {
        return new StudyImportExport()
                .setStudy(StudyTransformer.fromStudyDTO_V1(dto.getStudy()))
                .setStudyGroups(transform(dto.getStudyGroups(), StudyGroupTransformer::fromStudyGroupDTO_V1))
                .setObservations(transform(dto.getObservations(), ObservationTransformer::fromObservationDTO_V1))
                .setInterventions(transform(dto.getInterventions(), InterventionTransformer::fromInterventionDTO_V1))
                .setTriggers(
                        dto.getInterventions().stream().collect(Collectors.toMap(
                                InterventionDTO::getInterventionId,
                                interventionDTO ->
                                        TriggerTransformer.fromTriggerDTO_V1(interventionDTO.getTrigger())
                        ))
                )
                .setActions(
                        dto.getInterventions().stream().collect(Collectors.toMap(
                                InterventionDTO::getInterventionId,
                                interventionDTO ->
                                        transform(interventionDTO.getActions(), ActionTransformer::fromActionDTO_V1)
                        ))
                )
                .setParticipants(transform(dto.getParticipants(), ImportExportTransformer::fromParticipantDTO_V1))
                .setIntegrations(transform(dto.getIntegrations(), ImportExportTransformer::fromIntegrationExportDTO_V1));
    }

    public static StudyImportExportDTO toStudyImportExportDTO_V1(StudyImportExport studyImportExport) {
        return new StudyImportExportDTO()
                .study(StudyTransformer.toStudyDTO_V1(studyImportExport.getStudy()))
                .studyGroups(transform(studyImportExport.getStudyGroups(), StudyGroupTransformer::toStudyGroupDTO_V1))
                .observations(transform(studyImportExport.getObservations(), ObservationTransformer::toObservationDTO_V1))
                .interventions(transform(studyImportExport.getInterventions(), intervention ->
                    InterventionTransformer.toInterventionDTO_V1(intervention)
                            .trigger(
                                    TriggerTransformer.toTriggerDTO_V1(
                                            studyImportExport.getTriggers().get(intervention.getInterventionId())
                                    )
                            )
                            .actions(
                                    transform(
                                            studyImportExport.getActions().get(intervention.getInterventionId()),
                                            ActionTransformer::toActionDTO_V1
                                    )
                            )
                ))
                .participants(transform(studyImportExport.getParticipants(), ImportExportTransformer::toParticipantDTO_V1))
                .integrations(transform(studyImportExport.getIntegrations(), ImportExportTransformer::toIntegrationInfoDTO_V1));
    }

    private static ParticipantInfoDTO toParticipantDTO_V1(StudyImportExport.ParticipantInfo participant) {
        return new ParticipantInfoDTO()
                .studyGroup(participant.groupId());
    }

    private static StudyImportExport.ParticipantInfo fromParticipantDTO_V1(ParticipantInfoDTO participant) {
        return new StudyImportExport.ParticipantInfo(participant.getStudyGroup());
    }

    private static <S, T> List<T> transform(List<S> list, Function<S, T> transformer) {
        if (list == null) { return List.of(); }
        return list.stream().map(transformer).toList();
    }

    private static IntegrationInfoDTO toIntegrationInfoDTO_V1(IntegrationInfo integration) {
        return new IntegrationInfoDTO()
                .name(integration.name())
                .observationId(integration.observationId());
    }

    private static IntegrationInfo fromIntegrationExportDTO_V1(IntegrationInfoDTO integration) {
        return new IntegrationInfo(integration.getName(), integration.getObservationId());
    }
}
