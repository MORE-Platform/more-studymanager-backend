package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.ParticipationData;
import io.redlink.more.studymanager.model.StudyGroup;
import org.springframework.stereotype.Service;

import java.util.*;

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
        List<ParticipationData> incompleteParticipationDataList = elasticService.getParticipationData(studyId);
        List<ParticipationData> participationDataList = new ArrayList<>();
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
}