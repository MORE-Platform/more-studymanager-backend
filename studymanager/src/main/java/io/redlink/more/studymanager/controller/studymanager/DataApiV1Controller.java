/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.*;
import io.redlink.more.studymanager.api.v1.webservices.DataApi;
import io.redlink.more.studymanager.audit.Audited;
import io.redlink.more.studymanager.controller.RequiresStudyRole;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.transformer.StudyDataTransformer;
import io.redlink.more.studymanager.service.DataProcessingService;
import java.time.Instant;
import java.util.*;

import io.redlink.more.studymanager.service.ObservationService;
import io.redlink.more.studymanager.service.ParticipantService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.crypto.Data;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class DataApiV1Controller implements DataApi {

    private final DataProcessingService dataProcessingService;
    private final ParticipantService participantService;
    private final ObservationService observationService;

    public DataApiV1Controller(
            DataProcessingService dataProcessingService,
            ParticipantService participantService,
            ObservationService observationService
    ){
        this.dataProcessingService = dataProcessingService;
        this.participantService = participantService;
        this.observationService = observationService;
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_VIEWER})
    @Audited
    public ResponseEntity<List<DataPointDTO>> getDataPoints(
            Long studyId, Integer size, Integer observationId, Integer participantId, Instant date
    ) {
        if(studyId == null) {
            //the below code breaks down if the studyId is not set ... so better be save
            return ResponseEntity.badRequest().build();
        }
        //NOTE: The maps avoid to lookup participants and observations referenced in multiple data points twice
        Map<Integer, String> participantNames = new HashMap<>();
        Map<Integer, String> observationNames = new HashMap<>();
        return ResponseEntity.ok().body(
            dataProcessingService.getDataPoints(studyId, size, observationId, participantId, date).stream()
                    .map( sdp -> new DataPointDTO()
                        .observationId(sdp.getObservationId())
                        .observation(observationNames.computeIfAbsent(sdp.getObservationId(), id -> getObservationName(studyId, id)))
                        .participantId(sdp.getParticipantId())
                        .participant(participantNames.computeIfAbsent(sdp.getParticipantId(), id -> getParticipantName(studyId, id)))
                        .time(sdp.getTime())
                        .data(sdp.getData())
                    )
                    .toList()
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_VIEWER})
    @Audited
    public ResponseEntity<List<ParticipationDataDTO>> getParticipationData(Long studyId){
        return ResponseEntity.ok().body(
                dataProcessingService.getParticipationData(studyId).stream()
                        .map(StudyDataTransformer::toParticipationDataDTO_V1)
                        .toList()
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_VIEWER})
    @Audited
    public ResponseEntity<ObservationDataViewDataDTO> getObservationViewData(Long studyId, Integer observationId, String viewName, Integer studyGroupId, Integer participantId, Instant from, Instant to) {
        return ResponseEntity.ok().body(
                StudyDataTransformer.toObservationDataViewDataDTO(
                        dataProcessingService.getDataView(studyId, observationId, viewName, studyGroupId, participantId, from, to)
                )
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_VIEWER})
    @Audited
    public ResponseEntity<List<ObservationDataViewDTO>> listObservationViews(Long studyId, Integer observationId) {
        return ResponseEntity.ok().body(
                Arrays.stream(dataProcessingService.listDataViews(studyId, observationId))
                        .map(StudyDataTransformer::toObservationDataViewDTO)
                        .toList()
        );
    }

    private String getParticipantName(Long studyId, int participantId) {
        return Optional.ofNullable(participantService.getParticipant(studyId, participantId))
                .map(Participant::getAlias)
                .orElse("<unknown>");
    }

    private String getObservationName(Long studyId, int observationId) {
        return observationService.getObservation(studyId, observationId)
                .map(Observation::getTitle)
                .orElse("<unknown>");
    }

}
