package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.ParticipationData;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataProcessingService {
    private final ObservationService observationService;
    private final ParticipantService participantService;
    private final ElasticService elasticService;

    public DataProcessingService(ObservationService observationService, ParticipantService participantService, ElasticService elasticService){
        this.observationService = observationService;
        this.participantService = participantService;
        this.elasticService = elasticService;

    }

    public List<ParticipationData> getParticipationData(Long studyId){
        List<ParticipationData> participationDataList = elasticService.getParticipationData(studyId);
        List<Observation> observationList = observationService.listObservations(studyId);
        List<Participant> participantList = participantService.listParticipants(studyId);

        Map<Integer, List<Participant>> participantsByObservationId = new HashMap<>();
        for(Observation observation : observationList) {
            if(observation.getStudyGroupId() == null){
                participantsByObservationId.put(observation.getObservationId(), participantList);
            }else{
                participantsByObservationId.put(observation.getObservationId(), participantList
                        .stream().filter(p -> (observation.getStudyGroupId().equals(p.getStudyGroupId())))
                        .toList());
            }
        }
        for(Integer observationId : participantsByObservationId.keySet()){
            for(Participant participant : participantsByObservationId.get(observationId)){
                if(participationDataList.stream()
                        .filter(p -> (p.observationId().equals(observationId) &&
                                p.studyGroupId().equals(participant.getStudyGroupId()) &&
                                p.participantId().equals(participant.getParticipantId()))).toList().isEmpty())
                    participationDataList.add(new ParticipationData(observationId,
                                    participant.getParticipantId(),
                                    participant.getStudyGroupId(),
                                    false,
                                    null)
                    );
            }
        }
        Collections.sort(participationDataList);
        return participationDataList;
    }
}