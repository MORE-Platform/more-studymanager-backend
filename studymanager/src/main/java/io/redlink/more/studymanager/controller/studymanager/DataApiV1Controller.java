/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.DataPointDTO;
import io.redlink.more.studymanager.api.v1.model.ObservationDataViewDTO;
import io.redlink.more.studymanager.api.v1.model.ObservationDataViewDataDTO;
import io.redlink.more.studymanager.api.v1.model.ParticipationDataDTO;
import io.redlink.more.studymanager.api.v1.webservices.DataApi;
import io.redlink.more.studymanager.controller.RequiresStudyRole;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.transformer.StudyDataTransformer;
import io.redlink.more.studymanager.service.DataProcessingService;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class DataApiV1Controller implements DataApi {

    private final DataProcessingService dataProcessingService;

    public DataApiV1Controller(DataProcessingService dataProcessingService){ this.dataProcessingService = dataProcessingService; }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_VIEWER})
    public ResponseEntity<List<DataPointDTO>> getDataPoints(
            Long studyId, Integer size, Integer observationId, Integer participantId, OffsetDateTime date
    ) {
        return ResponseEntity.ok().body(
                dataProcessingService.getDataPoints(studyId, size, observationId, participantId, date)
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_VIEWER})
    public ResponseEntity<List<ParticipationDataDTO>> getParticipationData(Long studyId){
        return ResponseEntity.ok().body(
                dataProcessingService.getParticipationData(studyId).stream()
                        .map(StudyDataTransformer::toParticipationDataDTO_V1)
                        .toList()
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_VIEWER})
    public ResponseEntity<ObservationDataViewDataDTO> getObservationViewData(Long studyId, Integer observationId, String viewName, Integer studyGroupId, Integer participantId, OffsetDateTime from, OffsetDateTime to) {
        return ResponseEntity.ok().body(
                StudyDataTransformer.toObservationDataViewDataDTO(
                        dataProcessingService.getDataView(studyId, observationId, viewName, studyGroupId, participantId, from, to)
                )
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_VIEWER})
    public ResponseEntity<List<ObservationDataViewDTO>> listObservationViews(Long studyId, Integer observationId) {
        return ResponseEntity.ok().body(
                Arrays.stream(dataProcessingService.listDataViews(studyId, observationId))
                        .map(StudyDataTransformer::toObservationDataViewDTO)
                        .toList()
        );
    }
}
