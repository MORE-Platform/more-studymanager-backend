package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.repository.StudyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudyService {

    private final StudyRepository studyRepository;

    public StudyService(StudyRepository studyRepository) {
        this.studyRepository = studyRepository;
    }

    public Study createStudy(Study study) {
        return studyRepository.insert(study);
    }

    public List<Study> listStudies() {
        return studyRepository.listStudyOrderByModifiedDesc();
    }

    public Study getStudy(Long studyId) {
        return Optional.ofNullable(studyRepository.getById(studyId))
                .orElseThrow(() -> NotFoundException.Study(studyId));
    }

    public Study updateStudy(Study study) {
        return null;
    }

    public void deleteStudy(Long studyId) {

    }

    public void setStatus(Study.Status status) {
        //set status and start/end if necessary
    }
}
