/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.api.v1.model.DataPointDTO;
import io.redlink.more.studymanager.core.io.Timeframe;
import io.redlink.more.studymanager.core.ui.DataView;
import io.redlink.more.studymanager.core.ui.DataViewInfo;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.StudyGroup;
import io.redlink.more.studymanager.model.data.ParticipationData;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class DataProcessingService {
    private final ObservationService observationService;
    private final ParticipantService participantService;
    private final StudyGroupService studyGroupService;
    private final ElasticService elasticService;

    public DataProcessingService(ObservationService observationService, ParticipantService participantService, ElasticService elasticService, StudyGroupService studyGroupService){
        this.observationService = observationService;
        this.participantService = participantService;
        this.studyGroupService = studyGroupService;
        this.elasticService = elasticService;

    }

    public List<ParticipationData> getParticipationData(Long studyId){
        List<Observation> observationList = observationService.listObservations(studyId);
        List<Participant> participantList = participantService.listParticipants(studyId);
        List<StudyGroup> studyGroupList = studyGroupService.listStudyGroups(studyId);

        Map<Observation, List<Participant>> participantsByObservation = new HashMap<>();
        for(Observation observation : observationList) {
            if(observation.getStudyGroupId() == null){
                participantsByObservation.put(observation, participantList);
            }else{
                participantsByObservation.put(observation, participantList
                        .stream().filter(p -> (observation.getStudyGroupId().equals(p.getStudyGroupId())))
                        .toList());
            }
        }
        Map<Integer, Observation> observationById = new HashMap<>();
        Map<Integer, Participant> participantById = new HashMap<>();
        Map<Integer, StudyGroup> studyGroupById = new HashMap<>();
        for(Observation observation : observationList){
            observationById.put(observation.getObservationId(), observation);
        }
        for(Participant participant : participantList){
            participantById.put(participant.getParticipantId(), participant);
        }
        for(StudyGroup studyGroup : studyGroupList){
            studyGroupById.put(studyGroup.getStudyGroupId(), studyGroup);
        }

        List<ParticipationData> incompleteParticipationDataList = elasticService.getParticipationData(studyId)
                .stream().filter(p -> observationById.get(p.observationNamedId().id()) != null).toList();
        List<ParticipationData> participationDataList = new ArrayList<>();

        ParticipationData.NamedId studyGroup;
        for(ParticipationData participationData : incompleteParticipationDataList){
            studyGroup = null;
            if(participationData.studyGroupNamedId() != null)
                studyGroup = new ParticipationData.NamedId(
                        participationData.studyGroupNamedId().id(),
                        studyGroupById.get(participationData.studyGroupNamedId().id()).getTitle());
            participationDataList.add(new ParticipationData(
                    new ParticipationData.NamedId(participationData.observationNamedId().id(), observationById.get(participationData.observationNamedId().id()).getTitle()),
                    observationById.get(participationData.observationNamedId().id()).getType(),
                    new ParticipationData.NamedId(participationData.participantNamedId().id(), participantById.get(participationData.participantNamedId().id()).getAlias()),
                    studyGroup,
                    participationData.dataReceived(),
                    participationData.lastDataReceived()
            ));
        }

        for(Observation observation : participantsByObservation.keySet()){
            for(Participant participant : participantsByObservation.get(observation)){
                if(participationDataList.stream()
                        .filter(p -> (
                                p.observationNamedId().id() == (observation.getObservationId()) &&
                                        (p.studyGroupNamedId() != null
                                                ? (participant.getStudyGroupId() != null && p.studyGroupNamedId().id() == participant.getStudyGroupId())
                                                : participant.getStudyGroupId() == null) &&
                                        p.participantNamedId().id() == (participant.getParticipantId()))).toList().isEmpty()) {
                    studyGroup = null;
                    if(participant.getStudyGroupId() != null)
                        studyGroup = new ParticipationData.NamedId(participant.getStudyGroupId(), studyGroupById.get(participant.getStudyGroupId()).getTitle());
                    participationDataList.add(new ParticipationData(
                            new ParticipationData.NamedId(observation.getObservationId(), observation.getTitle()),
                            observation.getType(),
                            new ParticipationData.NamedId(participant.getParticipantId(), participant.getAlias()),
                            studyGroup,
                            false,
                            null)
                    );
                }
            }
        }
        Collections.sort(participationDataList);
        return participationDataList;
    }

    public DataViewInfo[] listDataViews(Long studyId, Integer observationId) {
        return observationService.listDataViews(studyId, observationId);
    }

    public DataView getDataView(Long studyId, Integer observationId, String viewName, Integer studyGroupId, Integer participantId, Instant from, Instant to) {
        if (participantId != null) {
            return observationService.queryData(studyId, observationId, viewName, null, participantId, from != null && to != null ? new Timeframe(from, to) : null);
        } else if (studyGroupId != null) {
            return observationService.queryData(studyId, observationId, viewName, studyGroupId, null, from != null && to != null ? new Timeframe(from, to) : null);
        }
        return observationService.queryData(studyId, observationId, viewName, studyGroupId, participantId, from != null && to != null ? new Timeframe(from, to) : null);
    }

    public List<DataPointDTO> getDataPoints(Long studyId, Integer size, Integer observationId, Integer participantId, Instant date) {
        try {
            return elasticService.listDataPoints(studyId, participantId, observationId, toIsoString(date), size).stream()
                    .map(dp -> new DataPointDTO()
                            .observation(getObservationName(studyId, dp.getObservationId()))
                            .observationId(dp.getObservationId())
                            .participant(getParticipantName(studyId, dp.getParticipantId()))
                            .participantId(dp.getParticipantId())
                            .time(dp.getTime())
                            .data(dp.getData())
                    )
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String toIsoString(Instant date) {
        return date != null ? DateTimeFormatter.ISO_INSTANT.format(date) : null;
    }

    @Cacheable("participants")
    public String getParticipantName(Long studyId, int participantId) {
        return Optional.ofNullable(participantService.getParticipant(studyId, participantId))
                .map(Participant::getAlias)
                .orElse("<unknown>");
    }

    @Cacheable("observations")
    public String getObservationName(Long studyId, int observationId) {
        return observationService.getObservation(studyId, observationId)
                .map(Observation::getTitle)
                .orElse("<unknown>");
    }
}
