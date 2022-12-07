package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.User;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.generator.RandomTokenGenerator;
import io.redlink.more.studymanager.repository.ParticipantRepository;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.stereotype.Service;

import java.util.List;

import static io.redlink.more.studymanager.model.Participant.Status.ABANDONED;
import static io.redlink.more.studymanager.model.Participant.Status.KICKED_OUT;
import static io.redlink.more.studymanager.model.Participant.Status.LOCKED;

@Service
public class ParticipantService {

    private static final Set<StudyRole> READ_ROLES = EnumSet.of(StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR);
    private static final Set<StudyRole> WRITE_ROLES = READ_ROLES;

    private final ParticipantRepository participantRepository;
    private final StudyPermissionService studyPermissionService;


    public ParticipantService(ParticipantRepository repository, StudyPermissionService studyPermissionService) {
        this.participantRepository = repository;
        this.studyPermissionService = studyPermissionService;
    }

    public Participant createParticipant(Participant participant, User user) {
        studyPermissionService.assertAnyRole(participant.getStudyId(), user.id(), WRITE_ROLES);
        participant.setRegistrationToken(RandomTokenGenerator.generate());
        return participantRepository.insert(participant);
    }

    public List<Participant> listParticipants(Long studyId, User user) {
        if (user != null) {
            studyPermissionService.assertAnyRole(studyId, user.id(), READ_ROLES);
        }
        return participantRepository.listParticipants(studyId);
    }

    public Participant getParticipant(Long studyId, Integer participantId, User user) {
        studyPermissionService.assertAnyRole(studyId, user.id(), READ_ROLES);
        return participantRepository.getByIds(studyId, participantId);
    }

    public void deleteParticipant(Long studyId, Integer participantId, User user) {
        studyPermissionService.assertAnyRole(studyId, user.id(), WRITE_ROLES);
        participantRepository.deleteParticipant(studyId, participantId);
    }

    public Participant updateParticipant(Participant participant, User user) {
        studyPermissionService.assertAnyRole(participant.getStudyId(), user.id(), WRITE_ROLES);
        return participantRepository.update(participant);
    }

    public void setStatus(Long studyId, Integer participantId, Participant.Status status, User user) {
        studyPermissionService.assertAnyRole(studyId, user.id(), WRITE_ROLES);
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
