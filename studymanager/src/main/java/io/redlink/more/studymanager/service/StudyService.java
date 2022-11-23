package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.repository.StudyRepository;
import io.redlink.more.studymanager.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudyService {

    private final StudyRepository studyRepository;
    private final UserRepository userRepo;
    private final InterventionService interventionService;

    public StudyService(StudyRepository studyRepository, UserRepository userRepo, InterventionService interventionService) {
        this.studyRepository = studyRepository;
        this.userRepo = userRepo;
        this.interventionService = interventionService;
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
        return studyRepository.update(study);
    }

    public void deleteStudy(Long studyId) {
        studyRepository.deleteById(studyId);
    }

    public void setStatus(Long studyId, Study.Status status) {
        Study study = getStudy(studyId);
        if(status.equals(Study.Status.DRAFT)) {
            throw BadRequestException.StateChange(study.getStudyState(), Study.Status.DRAFT);
        }
        if(study.getStudyState().equals(Study.Status.CLOSED)) {
            throw BadRequestException.StateChange(Study.Status.CLOSED, status);
        }
        if(study.getStudyState().equals(status)) {
            throw BadRequestException.StateChange(study.getStudyState(), status);
        }
        studyRepository.setStateById(studyId, status);

        if(status.equals(Study.Status.ACTIVE)) {
            interventionService.activateInterventionsFor(study);
        } else {
            interventionService.deactivateInterventionsFor(study);
        }
    }
}
