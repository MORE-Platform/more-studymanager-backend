package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.repository.ParticipantRepository;
import io.redlink.more.studymanager.repository.StudyRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class ImportExportService {

    private final ParticipantRepository participantRepository;
    private final StudyRepository studyRepository;
    public ImportExportService(ParticipantRepository participantRepository, StudyRepository studyRepository) {
        this.participantRepository = participantRepository;
        this.studyRepository = studyRepository;
    }

    public ByteArrayResource exportParticipants(Long studyId) {
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
