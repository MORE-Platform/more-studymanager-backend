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

        Map<Integer, String> observationTitleById = new HashMap<>();
        Map<Integer, String> participantAliasById = new HashMap<>();
        Map<Integer, String> studyGroupTitleById = new HashMap<>();
        for(Observation observation : observationList){
            observationTitleById.put(observation.getObservationId(), observation.getTitle());
        }
        for(Participant participant : participantList){
            participantAliasById.put(participant.getParticipantId(), participant.getAlias());
        }
        for(StudyGroup studyGroup : studyGroupList){
            studyGroupTitleById.put(studyGroup.getStudyGroupId(), studyGroup.getTitle());
        }
        for(ParticipationData participationData : incompleteParticipationDataList){
            participationDataList.add(new ParticipationData(
                    new ParticipationData.NamedId(participationData.observationData().id(), observationTitleById.get(participationData.observationData().id())),
                    new ParticipationData.NamedId(participationData.participantData().id(), participantAliasById.get(participationData.participantData().id())),
                    new ParticipationData.NamedId(participationData.studyGroupData().id(), studyGroupTitleById.get(participationData.studyGroupData().id())),
                    participationData.dataReceived(),
                    participationData.lastDataReceived()
            ));
        }

        for(Observation observation : participantsByObservation.keySet()){
            for(Participant participant : participantsByObservation.get(observation)){
                if(participationDataList.stream()
                        .filter(p -> (p.observationData().id() == (observation.getObservationId()) &&
                                p.studyGroupData().id() == (participant.getStudyGroupId()) &&
                                p.participantData().id() == (participant.getParticipantId()))).toList().isEmpty())
                    participationDataList.add(new ParticipationData(
                            new ParticipationData.NamedId(observation.getObservationId(), observation.getTitle()),
                            new ParticipationData.NamedId(participant.getParticipantId(), participant.getAlias()),
                            new ParticipationData.NamedId(participant.getStudyGroupId(), studyGroupTitleById.get(participant.getStudyGroupId())),
                            false,
                            null)
                    );
            }
        }
        Collections.sort(participationDataList);
        return participationDataList;
    }
}