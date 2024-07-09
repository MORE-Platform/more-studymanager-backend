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
import io.redlink.more.studymanager.model.data.ParticipationData;

import java.util.List;
import java.util.stream.Collectors;

public class StudyDataTransformer {

    private StudyDataTransformer(){}

    public static ParticipationDataDTO toParticipationDataDTO_V1(ParticipationData participationData){
        return new ParticipationDataDTO()
                .observationNamedId(toIdTitleDTO_V1(participationData.observationNamedId()))
                .observationType(participationData.observationType())
                .participantNamedId(toIdTitleDTO_V1(participationData.participantNamedId()))
                .studyGroupNamedId(toIdTitleDTO_V1(participationData.studyGroupNamedId()))
                .dataReceived(participationData.dataReceived())
                .lastDataReceived(Transformers.toOffsetDateTime(participationData.lastDataReceived()));
    }
    public static IdTitleDTO toIdTitleDTO_V1(ParticipationData.NamedId idTitle){
        if(idTitle == null)
            return null;
        return new IdTitleDTO()
                .id(idTitle.id())
                .title(idTitle.title());
    }

    public static ObservationDataViewDataDTO toObservationDataViewDataDTO(DataView dataView){
        return new ObservationDataViewDataDTO()
                .view(toObservationDataViewDTO(dataView.viewInfo()))
                .chartType(toChartTypeEnumDTO(dataView.chartType()))
                .labels(dataView.data().labels())
                .data(toObservationDataViewDataRowDTO(dataView.data().rows()));
    }

    public static ObservationDataViewDTO toObservationDataViewDTO(DataViewInfo dataViewInfo) {
        return new ObservationDataViewDTO()
                .name(dataViewInfo.name())
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
