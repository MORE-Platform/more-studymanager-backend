package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.generator.RandomTokenGenerator;
import io.redlink.more.studymanager.repository.ParticipantRepository;
import java.util.EnumSet;
import org.springframework.stereotype.Service;

import java.util.List;

import static io.redlink.more.studymanager.model.Participant.Status.ABANDONED;
import static io.redlink.more.studymanager.model.Participant.Status.KICKED_OUT;
import static io.redlink.more.studymanager.model.Participant.Status.LOCKED;

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
        if (EnumSet.of(ABANDONED, KICKED_OUT, LOCKED).contains(status)) {
            participantRepository.cleanupParticipant(studyId, participantId);
        }
    }

    public void alignParticipantsWithStudyState(Study study) {
        if (study.getStudyState() == Study.Status.CLOSED) {
            participantRepository.cleanupParticipants(study.getStudyId());
        }
    }
}
