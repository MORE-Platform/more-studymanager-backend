package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.User;

import java.io.InputStream;
import java.util.EnumSet;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

@Service
public class ImportExportService {

    private final ParticipantService participantService;
    private final StudyService studyService;
    private final StudyPermissionService studyPermissionService;

    public ImportExportService(ParticipantService participantService, StudyService studyService,
                               StudyPermissionService studyPermissionService) {
        this.participantService = participantService;
        this.studyService = studyService;
        this.studyPermissionService = studyPermissionService;
    }

    public Resource exportParticipants(Long studyId, User user) {
        studyPermissionService.assertAnyRole(studyId, user.id(), EnumSet.of(StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR));

        List<Participant> participantList = participantService.listParticipants(studyId, user);
        Study study = studyService.getStudy(studyId, user)
                .orElseThrow(() -> new NotFoundException("study", studyId));
        StringBuilder str = new StringBuilder("STUDYID;TITLE;ALIAS;PARTICIPANTID;REGISTRATIONTOKEN\n");
        participantList.forEach(p -> str.append(writeToParticipantCsv(p, study)));
        return new ByteArrayResource(str.toString().getBytes(StandardCharsets.UTF_8));
    }

    public void importParticipants(Long studyId, User user, InputStream inputStream) {
        studyPermissionService.assertAnyRole(studyId, user.id(), EnumSet.of(StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR));

        var scanner = new Scanner(inputStream).useDelimiter("[\\r\\n]+");
        boolean isHeader = true;
        while (scanner.hasNext()) {
            String line = scanner.next();
            if(!isHeader && StringUtils.isNotBlank(line)) {
                participantService.createParticipant(new Participant().setStudyId(studyId).setAlias(line), user);
            } else {
                isHeader = false;
            }
        }
        scanner.close();
    }

    private String writeToParticipantCsv(Participant participant, Study study) {
        return "%d;%s;%s;%d;%s\n".formatted(study.getStudyId(), study.getTitle(), participant.getAlias(),
                participant.getParticipantId(), participant.getRegistrationToken());
    }

}
