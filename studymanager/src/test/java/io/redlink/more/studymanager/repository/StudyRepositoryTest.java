package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.Study;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@ActiveProfiles("test-containers-flyway")
class StudyRepositoryTest {
    @Autowired
    private StudyRepository studyRepository;

    @BeforeEach
    void deleteAll() {
        studyRepository.clear();
    }

    @Test
    @DisplayName("Study us inserted in database and returned")
    public void testInsert() {
        Study study = new Study()
                .setTitle("some title");

        Study studyResponse = studyRepository.insert(study);

        assertThat(studyResponse.getStudyId()).isNotNull();
        assertThat(studyResponse.getTitle()).isEqualTo(study.getTitle());
        assertThat(studyResponse.getStudyState()).isEqualTo(Study.Status.DRAFT);
    }

}
