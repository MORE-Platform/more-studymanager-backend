/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.exception.BadStudyStateException;
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

    public Study assertStudyNotInState(final Study study, Set<Study.Status> states) {
        assertStudyNotInState(study.getStudyId(), states);
        return study;
    }

    public Long assertStudyNotInState(Long studyId, Study.Status... states) {
        return assertStudyNotInState(studyId, Set.of(states));
    }

    public Long assertStudyNotInState(Long studyId, Set<Study.Status> states) {
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
        if (studyId == null)
            throw new IllegalArgumentException("studyId must not be null");
        if (studyRepository.hasState(studyId, states))
            return studyId;
        throw BadStudyStateException.state();
    }
}
