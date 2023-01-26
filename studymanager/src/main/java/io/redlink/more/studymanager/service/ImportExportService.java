package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.User;

import java.io.InputStream;

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
    private final StudyStateService studyStateService;

    public ImportExportService(ParticipantService participantService, StudyService studyService, StudyStateService studyStateService) {
        this.participantService = participantService;
        this.studyService = studyService;
        this.studyStateService = studyStateService;
    }

    public Resource exportParticipants(Long studyId, User user) {
        List<Participant> participantList = participantService.listParticipants(studyId);
        Study study = studyService.getStudy(studyId, user)
                .orElseThrow(() -> new NotFoundException("study", studyId));
        StringBuilder str = new StringBuilder("STUDYID;TITLE;ALIAS;PARTICIPANTID;REGISTRATIONTOKEN\n");
        participantList.forEach(p -> str.append(writeToParticipantCsv(p, study)));
        return new ByteArrayResource(str.toString().getBytes(StandardCharsets.UTF_8));
    }

    public void importParticipants(Long studyId, InputStream inputStream) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        var scanner = new Scanner(inputStream).useDelimiter("[\\r\\n]+");
        boolean isHeader = true;
        while (scanner.hasNext()) {
            String line = scanner.next();
            if(!isHeader && StringUtils.isNotBlank(line)) {
                participantService.createParticipant(new Participant().setStudyId(studyId).setAlias(line));
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
