package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.RegistrationToken;
import io.redlink.more.studymanager.model.generator.RandomTokenGenerator;
import io.redlink.more.studymanager.repository.ParticipantRepository;
import io.redlink.more.studymanager.repository.RegistrationTokenRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final RegistrationTokenRepository registrationTokenRepository;

    public ParticipantService(ParticipantRepository repository, RegistrationTokenRepository registrationTokenRepository) {
        this.participantRepository = repository;
        this.registrationTokenRepository = registrationTokenRepository;
    }

    public Participant createParticipant(Participant participant) {
        participant.setRegistrationToken(RandomTokenGenerator.generate());
        Participant insertedParticipant = participantRepository.insert(participant);
        registrationTokenRepository.insert(new RegistrationToken()
                .setStudyId(insertedParticipant.getStudyId())
                .setParticipantId(insertedParticipant.getParticipantId())
                .setToken(participant.getRegistrationToken()));
        return insertedParticipant;
    }

    public List<Participant> listParticipants(Long studyId) {
        return participantRepository.listParticipants(studyId).stream().map(
                participant -> participant.setRegistrationToken(
                        registrationTokenRepository.getByIds(studyId, participant.getParticipantId()).getToken())
        ).toList();
    }

}
