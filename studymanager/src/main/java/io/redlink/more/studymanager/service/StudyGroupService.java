package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.StudyGroup;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.User;
import io.redlink.more.studymanager.repository.StudyGroupRepository;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudyGroupService {

    private static final Set<StudyRole> EDIT_ROLES = EnumSet.of(StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR);

    private final StudyGroupRepository repository;
    private final StudyPermissionService studyPermissionService;

    public StudyGroupService(StudyGroupRepository repository, StudyPermissionService studyPermissionService) {
        this.repository = repository;
        this.studyPermissionService = studyPermissionService;
    }

    public StudyGroup createStudyGroup(StudyGroup studyGroup, User user) {
        studyPermissionService.assertAnyRole(studyGroup.getStudyId(), user.id(), EDIT_ROLES);
        return this.repository.insert(studyGroup);
    }

    public List<StudyGroup> listStudyGroups(long studyId, User user) {
        studyPermissionService.assertAnyRole(studyId, user.id(), EDIT_ROLES);
        return this.repository.listStudyGroupsOrderedByStudyGroupIdAsc(studyId);
    }

    public StudyGroup getStudyGroup(long studyId, int studyGroupId, User user) {
        studyPermissionService.assertAnyRole(studyId, user.id(), EDIT_ROLES);
        return Optional.ofNullable(repository.getByIds(studyId, studyGroupId))
                .orElseThrow(() -> NotFoundException.StudyGroup(studyId, studyGroupId));
    }

    public StudyGroup updateStudyGroup(StudyGroup studyGroup, User user) {
        studyPermissionService.assertAnyRole(studyGroup.getStudyId(), user.id(), EDIT_ROLES);
        return this.repository.update(studyGroup);
    }

    public void deleteStudyGroup(long studyId, int studyGroupId, User user) {
        studyPermissionService.assertAnyRole(studyId, user.id(), EDIT_ROLES);
        this.repository.deleteById(studyId, studyGroupId);
    }
}
