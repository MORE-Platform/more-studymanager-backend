/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.*;
import io.redlink.more.studymanager.core.ui.DataView;
import io.redlink.more.studymanager.core.ui.DataViewInfo;
import io.redlink.more.studymanager.core.ui.DataViewRow;
import io.redlink.more.studymanager.model.Auditlog;
import io.redlink.more.studymanager.model.data.AuditlogData;
import io.redlink.more.studymanager.model.data.ParticipationData;
import java.util.List;
import java.util.stream.Collectors;

public final class StudyDataTransformer {

    private StudyDataTransformer(){}

    public static ParticipationDataDTO toParticipationDataDTO_V1(ParticipationData participationData){
        return new ParticipationDataDTO()
                .observationNamedId(toIdTitleDTO_V1(participationData.observationNamedId()))
                .observationType(participationData.observationType())
                .participantNamedId(toIdTitleDTO_V1(participationData.participantNamedId()))
                .studyGroupNamedId(toIdTitleDTO_V1(participationData.studyGroupNamedId()))
                .dataReceived(participationData.dataReceived())
                .lastDataReceived(participationData.lastDataReceived());
    }
    public static IdTitleDTO toIdTitleDTO_V1(ParticipationData.NamedId idTitle){
        if(idTitle == null)
            return null;
        return new IdTitleDTO()
                .id(idTitle.id())
                .title(idTitle.title());
    }

    public static AuditlogData toAuditlogData(Auditlog auditlog) {
        return new AuditlogData(
                auditlog.getStudyId(),
                auditlog.getAuditlogId(),
                auditlog.getUserId(),
                auditlog.getUserRoles(),
                auditlog.getUserName(),
                auditlog.getTimestamp(),
                auditlog.getAction(),
                auditlog.getState(),
                auditlog.getDetails()
        );
    }

    public static AuditlogDataDTO toAuditlogDataDTO_V1(AuditlogData auditlogData) {
        return new AuditlogDataDTO()
                .studyId(auditlogData.studyId())
                .auditlogId(auditlogData.auditlogId())
                .userId(auditlogData.userId())
                .userRoles(auditlogData.userRoles())
                .userName(auditlogData.userName())
                .timestamp(auditlogData.timestamp())
                .action(auditlogData.action())
                .state(auditlogData.state())
                .details(auditlogData.details());
    }
    public static List<AuditlogDataDTO> toAuditlogDataDTO_V1(List<AuditlogData> auditlogData){
        if (auditlogData == null || auditlogData.isEmpty()) {
            return List.of();
        }
        return auditlogData.stream()
                .map(StudyDataTransformer::toAuditlogDataDTO_V1)
                .toList();
    }


    public static ObservationDataViewDataDTO toObservationDataViewDataDTO(DataView dataView){
        var observationDataViewDataDTO = new ObservationDataViewDataDTO()
                .view(toObservationDataViewDTO(dataView.viewInfo()))
                .chartType(toChartTypeEnumDTO(dataView.chartType()));

        if (dataView.data() != null) {
            observationDataViewDataDTO
                    .labels(dataView.data().labels())
                    .data(toObservationDataViewDataRowDTO(dataView.data().rows()));
        }

        return observationDataViewDataDTO;
    }

    public static ObservationDataViewDTO toObservationDataViewDTO(DataViewInfo dataViewInfo) {
        return new ObservationDataViewDTO()
                .name(dataViewInfo.name())
                .label(dataViewInfo.label())
                .title(dataViewInfo.title())
                .description(dataViewInfo.description());
    }

    private static ObservationDataViewDataDTO.ChartTypeEnum toChartTypeEnumDTO(DataView.ChartType chartType) {
        return switch (chartType) {
            case LINE -> ObservationDataViewDataDTO.ChartTypeEnum.LINE;
            case BAR -> ObservationDataViewDataDTO.ChartTypeEnum.BAR;
            case PIE -> ObservationDataViewDataDTO.ChartTypeEnum.PIE;
        };
    }

    private static List<ObservationDataViewDataRowDTO> toObservationDataViewDataRowDTO(List<DataViewRow> dataViewRow) {
        return dataViewRow.stream()
                .map(row -> new ObservationDataViewDataRowDTO().label(row.label()).values(row.values()))
                .collect(Collectors.toList());
    }
}
