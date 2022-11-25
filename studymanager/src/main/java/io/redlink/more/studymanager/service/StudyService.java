package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.User;
import io.redlink.more.studymanager.repository.StudyAclRepository;
import io.redlink.more.studymanager.repository.StudyRepository;
import io.redlink.more.studymanager.repository.UserRepository;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class StudyService {

    private final StudyRepository studyRepository;
    private final StudyAclRepository aclRepository;
    private final UserRepository userRepo;
    private final InterventionService interventionService;

    public StudyService(StudyRepository studyRepository, StudyAclRepository aclRepository, UserRepository userRepo, InterventionService interventionService) {
        this.studyRepository = studyRepository;
        this.aclRepository = aclRepository;
        this.userRepo = userRepo;
        this.interventionService = interventionService;
    }

    public Study createStudy(Study study, User currentUser) {
        // TODO: Workaround until proper auth is available
        var user = userRepo.save(currentUser);
        var savedStudy = studyRepository.insert(study);
        aclRepository.setRoles(savedStudy.getStudyId(), user.id(), EnumSet.allOf(StudyRole.class));
        return savedStudy;
    }

    public List<Study> listStudies() {
        return studyRepository.listStudyOrderByModifiedDesc();
    }

    public List<Study> listStudies(User user) {
        return listStudies(user, EnumSet.allOf(StudyRole.class));
    }

    public List<Study> listStudies(User user, Set<StudyRole> allowedRoles) {
        return studyRepository.listStudiesByAclOrderByModifiedDesc(user, allowedRoles);
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
