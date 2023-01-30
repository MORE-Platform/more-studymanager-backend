package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.exception.BadStudyStateException;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.repository.StudyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudyStateServiceTest {

    @Mock
    StudyRepository studyRepository;

    @InjectMocks
    StudyStateService studyStateService;

    @Test
    void testAssertPasses() {
        Study study = new Study().setStudyId(1L);
        when(studyRepository.hasState(anyLong(), anySet())).thenAnswer(i -> i.getArgument(1, Set.class).contains(Study.Status.PAUSED));

        assertThat(studyStateService.assertStudyNotInState(study, Study.Status.DRAFT, Study.Status.CLOSED)).isEqualTo(study);
        assertThat(studyStateService.assertStudyNotInState(study, Set.of(Study.Status.DRAFT, Study.Status.ACTIVE))).isEqualTo(study);
        assertThat(studyStateService.assertStudyNotInState(study.getStudyId(), Study.Status.CLOSED)).isEqualTo(study.getStudyId());
        assertThat(studyStateService.assertStudyNotInState(study.getStudyId(), Set.of(Study.Status.DRAFT, Study.Status.CLOSED))).isEqualTo(study.getStudyId());

        assertThat(studyStateService.assertStudyState(study, Study.Status.PAUSED)).isEqualTo(study);
        assertThat(studyStateService.assertStudyState(study, Set.of(Study.Status.DRAFT, Study.Status.PAUSED))).isEqualTo(study);
        assertThat(studyStateService.assertStudyState(study.getStudyId(), Study.Status.PAUSED, Study.Status.ACTIVE)).isEqualTo(study.getStudyId());
        assertThat(studyStateService.assertStudyState(study.getStudyId(), Set.of(Study.Status.PAUSED, Study.Status.CLOSED))).isEqualTo(study.getStudyId());

        verify(studyRepository, times(8))
                .hasState(anyLong(), anySet());
    }

    @Test
    void testAssertFails() {
        Study study = new Study().setStudyId(1L);
        when(studyRepository.hasState(anyLong(), anySet())).thenAnswer(i -> i.getArgument(1, Set.class).contains(Study.Status.DRAFT));

        assertThrows(BadStudyStateException.class, () -> studyStateService.assertStudyNotInState(study, Study.Status.ACTIVE, Study.Status.DRAFT));
        assertThrows(BadStudyStateException.class, () -> studyStateService.assertStudyNotInState(study, Set.of(Study.Status.CLOSED, Study.Status.DRAFT)));
        assertThrows(BadStudyStateException.class, () -> studyStateService.assertStudyNotInState(study.getStudyId(), Study.Status.DRAFT));
        assertThrows(BadStudyStateException.class, () -> studyStateService.assertStudyNotInState(study.getStudyId(), Set.of(Study.Status.DRAFT)));

        assertThrows(BadStudyStateException.class, () -> studyStateService.assertStudyState(study, Study.Status.CLOSED, Study.Status.ACTIVE));
        assertThrows(BadStudyStateException.class, () -> studyStateService.assertStudyState(study, Set.of(Study.Status.CLOSED, Study.Status.ACTIVE)));
        assertThrows(BadStudyStateException.class, () -> studyStateService.assertStudyState(study.getStudyId(), Study.Status.PAUSED, Study.Status.CLOSED));
        assertThrows(BadStudyStateException.class, () -> studyStateService.assertStudyState(study.getStudyId(), Set.of(Study.Status.PAUSED, Study.Status.CLOSED)));

        verify(studyRepository, times(8))
                .hasState(anyLong(), anySet());
    }
}