/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.DataRowDTO;
import io.redlink.more.studymanager.api.v1.model.IdTitleDTO;
import io.redlink.more.studymanager.api.v1.model.MonitoringDataDTO;
import io.redlink.more.studymanager.api.v1.model.ParticipationDataDTO;
import io.redlink.more.studymanager.model.data.MonitoringData;
import io.redlink.more.studymanager.model.data.ParticipationData;

import java.util.List;

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

    public static MonitoringDataDTO toMonitoringDataDTO(MonitoringData monitoringData) {
        return new MonitoringDataDTO()
                .chartTitle(monitoringData.chartTitle())
                .chartType(monitoringData.chartType())
                .dataRows(toDataRowsDTO(monitoringData.dataRows()));
    }

    public static List<DataRowDTO> toDataRowsDTO(List<MonitoringData.DataRow> dataRows) {
        return dataRows
                .stream()
                .map(StudyDataTransformer::toDataRowDTO)
                .toList();
    }

    public static DataRowDTO toDataRowDTO(MonitoringData.DataRow dataRow) {
        return new DataRowDTO()
                .rowTitle(dataRow.rowTitle())
                .data(dataRow.data());
    }
}
