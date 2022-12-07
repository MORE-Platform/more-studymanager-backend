package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.User;
import io.redlink.more.studymanager.repository.ParticipantRepository;
import io.redlink.more.studymanager.repository.StudyRepository;
import java.util.EnumSet;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class ImportExportService {

    private final ParticipantRepository participantRepository;
    private final StudyRepository studyRepository;
    private final StudyPermissionService studyPermissionService;

    public ImportExportService(ParticipantRepository participantRepository, StudyRepository studyRepository,
                               StudyPermissionService studyPermissionService) {
        this.participantRepository = participantRepository;
        this.studyRepository = studyRepository;
        this.studyPermissionService = studyPermissionService;
    }

    public Resource exportParticipants(Long studyId, User user) {
        studyPermissionService.assertAnyRole(studyId, user.id(), EnumSet.of(StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR));

        List<Participant> participantList = participantRepository.listParticipants(studyId);
        Study study = studyRepository.getById(studyId)
                .orElseThrow(() -> new NotFoundException("study", studyId));
        StringBuilder str = new StringBuilder("STUDYID;TITLE;ALIAS;PARTICIPANTID;REGISTRATIONTOKEN\n");
        participantList.forEach(p -> str.append(writeToParticipantCsv(p, study)));
        return new ByteArrayResource(str.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String writeToParticipantCsv(Participant participant, Study study) {
        return "%d;%s;%s;%d;%s\n".formatted(study.getStudyId(), study.getTitle(), participant.getAlias(),
                participant.getParticipantId(), participant.getRegistrationToken());
    }

}
