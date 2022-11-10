package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.generator.RandomTokenGenerator;
import io.redlink.more.studymanager.repository.ParticipantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParticipantService {

    private final ParticipantRepository participantRepository;

    public ParticipantService(ParticipantRepository repository) {
        this.participantRepository = repository;
    }

    public Participant createParticipant(Participant participant) {
        participant.setRegistrationToken(RandomTokenGenerator.generate());
        return participantRepository.insert(participant);
    }

    public List<Participant> listParticipants(Long studyId) {
        return participantRepository.listParticipants(studyId);
    }

    public Participant getParticipant(Long studyId, Integer participantId) {
        return participantRepository.getByIds(studyId, participantId);
    }

    public void deleteParticipant(Long studyId, Integer participantId) {
        participantRepository.deleteParticipant(studyId, participantId);
    }

    public Participant updateParticipant(Participant participant) {
        return participantRepository.update(participant);
    }

    public void setStatus(Long studyId, Integer participantId, Participant.Status status) {
        participantRepository.setStatusByIds(studyId, participantId, status);
    }

}
