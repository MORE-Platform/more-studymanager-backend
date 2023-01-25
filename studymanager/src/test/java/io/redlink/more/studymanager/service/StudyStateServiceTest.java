package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.repository.StudyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
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
    void testAssert(){
        Study study = new Study().setStudyId(1L);
        when(studyRepository.assertStudyState(anyLong(), anySet())).thenReturn(study.getStudyId());

        assertThat(studyStateService.assertStudyNotInState(study, Study.Status.DRAFT)).isEqualTo(study);
        assertThat(studyStateService.assertStudyNotInState(study, Set.of(Study.Status.DRAFT))).isEqualTo(study);
        assertThat(studyStateService.assertStudyNotInState(study.getStudyId(), Study.Status.DRAFT)).isEqualTo(study.getStudyId());
        assertThat(studyStateService.assertStudyNotInState(study.getStudyId(), Set.of(Study.Status.DRAFT))).isEqualTo(study.getStudyId());

        assertThat(studyStateService.assertStudyNotInState(study, Study.Status.DRAFT)).isEqualTo(study);
        assertThat(studyStateService.assertStudyNotInState(study, Set.of(Study.Status.DRAFT))).isEqualTo(study);
        assertThat(studyStateService.assertStudyNotInState(study.getStudyId(), Study.Status.DRAFT)).isEqualTo(study.getStudyId());
        assertThat(studyStateService.assertStudyNotInState(study.getStudyId(), Set.of(Study.Status.DRAFT))).isEqualTo(study.getStudyId());

        verify(studyRepository, times(8))
                .assertStudyState(anyLong(), anySet());
    }


}