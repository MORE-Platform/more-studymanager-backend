package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.repository.StudyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StudyServiceTest {

    @Mock
    StudyRepository studyRepository;

    @InjectMocks
    StudyService studyService;

    @Test
    @DisplayName("When the study is saved it should return the study with id.")
    public void saveStudy() {
        Study study = new Study();
        study.setTitle("test study");

        when(studyRepository.insert(any(Study.class)))
                .thenReturn(new Study().setStudyId(1L).setTitle(study.getTitle()));

        Study studyResponse = studyService.createStudy(study);

        assertThat(studyResponse.getStudyId()).isEqualTo(1L);
        assertThat(studyResponse.getTitle()).isSameAs(study.getTitle());
    }
}
