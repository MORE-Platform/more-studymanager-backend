package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.MoreUser;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.StudyUserRoles;
import io.redlink.more.studymanager.model.User;
import io.redlink.more.studymanager.repository.StudyAclRepository;
import io.redlink.more.studymanager.repository.StudyRepository;
import io.redlink.more.studymanager.repository.UserRepository;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class StudyService {

    private final StudyRepository studyRepository;
    private final StudyAclRepository aclRepository;
    private final UserRepository userRepo;
    private final InterventionService interventionService;
    private final ParticipantService participantService;
    private final StudyPermissionService studyPermissionService;

    public StudyService(StudyRepository studyRepository, StudyAclRepository aclRepository, UserRepository userRepo,
                        InterventionService interventionService, ParticipantService participantService,
                        StudyPermissionService studyPermissionService) {
        this.studyRepository = studyRepository;
        this.aclRepository = aclRepository;
        this.userRepo = userRepo;
        this.interventionService = interventionService;
        this.participantService = participantService;
        this.studyPermissionService = studyPermissionService;
    }

    public Study createStudy(Study study, User currentUser) {
        // TODO: Workaround until proper auth is available
        var user = userRepo.save(currentUser);
        var savedStudy = studyRepository.insert(study);
        aclRepository.setRoles(savedStudy.getStudyId(), user.id(), EnumSet.allOf(StudyRole.class), null);
        return getStudy(savedStudy.getStudyId(), user).orElse(savedStudy);
    }

    public List<Study> listStudies(User user) {
        return listStudies(user, EnumSet.allOf(StudyRole.class));
    }

    public List<Study> listStudies(User user, Set<StudyRole> allowedRoles) {
        return studyRepository.listStudiesByAclOrderByModifiedDesc(user, allowedRoles);
    }

    public Optional<Study> getStudy(Long studyId, User user) {
        studyPermissionService.assertAnyRole(studyId, user.id(), EnumSet.allOf(StudyRole.class));
        return (studyRepository.getById(studyId, user))
                ;
    }

    public Optional<Study> updateStudy(Study study, User user) {
        studyPermissionService.assertAnyRole(study.getStudyId(), user.id(),
                EnumSet.of(StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR));

        return studyRepository.update(study, user);
    }

    public void deleteStudy(Long studyId, User user) {
        studyPermissionService.assertAnyRole(studyId, user.id(), StudyRole.STUDY_ADMIN);

        studyRepository.deleteById(studyId);
    }

    public void setStatus(Long studyId, Study.Status status, User user) {
        studyPermissionService.assertRole(studyId, user.id(), StudyRole.STUDY_ADMIN);

        Study study = getStudy(studyId, user)
                                .orElseThrow(() -> NotFoundException.Study(studyId));
        if (status.equals(Study.Status.DRAFT)) {
            throw BadRequestException.StateChange(study.getStudyState(), Study.Status.DRAFT);
        }
        if (study.getStudyState().equals(Study.Status.CLOSED)) {
            throw BadRequestException.StateChange(Study.Status.CLOSED, status);
        }
        if (study.getStudyState().equals(status)) {
            throw BadRequestException.StateChange(study.getStudyState(), status);
        }
        studyRepository.setStateById(studyId, status);

        interventionService.alignInterventionsWithStudyState(study);
        participantService.alignParticipantsWithStudyState(study);
    }

    public Map<MoreUser, Set<StudyRole>> getACL(Long studyId, User user) {
        studyPermissionService.assertAnyRole(studyId, user.id());

        return aclRepository.getACL(studyId);
    }

    public Optional<StudyUserRoles> setRolesForStudy(Long studyId, String userId, Set<StudyRole> roles,
                                                     User currentUser) {
        studyPermissionService.assertRole(studyId, currentUser.id(), StudyRole.STUDY_ADMIN);

        if (roles.isEmpty()) {
            aclRepository.clearRoles(studyId, userId);
            return Optional.empty();
        }

        aclRepository.setRoles(studyId, userId, roles, currentUser.id());
        return getRolesForStudy(studyId, userId, currentUser);
    }

    public Optional<StudyUserRoles> getRolesForStudy(Long studyId, String userId, User currentUser) {
        studyPermissionService.assertAnyRole(studyId, currentUser.id());

        return userRepo.getById(userId).map(user ->
                new StudyUserRoles(
                        user,
                        aclRepository.getRoleDetails(studyId, userId)
                )
        );
    }
}
