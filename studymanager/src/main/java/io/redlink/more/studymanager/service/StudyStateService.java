package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.repository.StudyRepository;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;

@Service
public class StudyStateService {

    private final StudyRepository studyRepository;

    public StudyStateService(StudyRepository studyRepository) {
        this.studyRepository = studyRepository;
    }

    public Study assertStudyNotInState(final Study study, Study.Status... states) {
        assertStudyNotInState(study.getStudyId(), Set.of(states));
        return study;
    }

    public Study assertStudyNotInState(final Study study, Set<Study.Status> states){
        assertStudyNotInState(study.getStudyId(), states);
        return study;
    }

    public Long assertStudyNotInState(Long studyId, Study.Status... states) {
        return assertStudyNotInState(studyId, Set.of(states));
    }

    public Long assertStudyNotInState(Long studyId, Set<Study.Status> states){
        return assertStudyState(studyId, EnumSet.complementOf(EnumSet.copyOf(states)));
    }

    public Study assertStudyState(final Study study, Study.Status... states) {
        assertStudyState(study.getStudyId(), Set.of(states));
        return study;
    }

    public Study assertStudyState(final Study study, Set<Study.Status> states) {
        assertStudyState(study.getStudyId(), states);
        return study;
    }

    public Long assertStudyState(Long studyId, Study.Status... states) {
        return assertStudyState(studyId, Set.of(states));
    }

    public Long assertStudyState(Long studyId, Set<Study.Status> states) {
        return studyRepository.assertStudyState(studyId, states);
    }
}
