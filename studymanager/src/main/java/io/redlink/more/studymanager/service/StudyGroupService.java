package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.StudyGroup;
import io.redlink.more.studymanager.repository.StudyGroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudyGroupService {

    StudyStateService studyStateService;
    private final StudyGroupRepository repository;

    public StudyGroupService(StudyStateService studyStateService, StudyGroupRepository repository) {
        this.studyStateService = studyStateService;
        this.repository = repository;
    }

    public StudyGroup createStudyGroup(StudyGroup studyGroup) {
        studyStateService.assertStudyNotInState(studyGroup.getStudyId(), Study.Status.CLOSED);
        return this.repository.insert(studyGroup);
    }

    public List<StudyGroup> listStudyGroups(long studyId) {
        return this.repository.listStudyGroupsOrderedByStudyGroupIdAsc(studyId);
    }

    public StudyGroup getStudyGroup(long studyId, int studyGroupId) {
        return Optional.ofNullable(repository.getByIds(studyId, studyGroupId))
                .orElseThrow(() -> NotFoundException.StudyGroup(studyId, studyGroupId));
    }

    public StudyGroup updateStudyGroup(StudyGroup studyGroup) {
        studyStateService.assertStudyNotInState(studyGroup.getStudyId(), Study.Status.CLOSED);
        return this.repository.update(studyGroup);
    }

    public void deleteStudyGroup(long studyId, int studyGroupId) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        this.repository.deleteById(studyId, studyGroupId);
    }
}
