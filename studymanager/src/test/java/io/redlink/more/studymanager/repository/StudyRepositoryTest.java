package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.Study;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
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
    @DisplayName("Study is inserted in database and returned")
    void testInsert() {
        Study study = new Study()
                .setTitle("some title")
                .setContactPerson("test person")
                .setContactEmail("test mail");

        Study studyResponse = studyRepository.insert(study);

        assertThat(studyResponse.getStudyId()).isNotNull();
        assertThat(studyResponse.getTitle()).isEqualTo(study.getTitle());
        assertThat(studyResponse.getStudyState()).isEqualTo(Study.Status.DRAFT);
        assertThat(studyResponse.getContactPerson()).isEqualTo(study.getContactPerson());
        assertThat(studyResponse.getContactEmail()).isEqualTo(study.getContactEmail());
    }
    @Test
    @DisplayName("Study is updated in database and returned")
    void testUpdate() {
        Study insert = new Study()
                .setTitle("some title")
                .setContactPerson("test person")
                .setContactEmail("test mail");

        Study inserted = studyRepository.insert(insert);

        Study update = new Study()
                .setStudyId(inserted.getStudyId())
                .setTitle("some new title")
                .setContactPerson("new test person")
                .setContactEmail("new test mail");

        Optional<Study> optUpdated = studyRepository.update(update, null);
        assertThat(optUpdated).isPresent();
        Study updated = optUpdated.get();


        Optional<Study> optQueried = studyRepository.getById(inserted.getStudyId());
        assertThat(optQueried).isPresent();
        Study queried = optQueried.get();

        assertThat(queried.getTitle()).isEqualTo(updated.getTitle());
        assertThat(queried.getStudyId()).isEqualTo(updated.getStudyId());
        assertThat(queried.getCreated()).isEqualTo(updated.getCreated());
        assertThat(queried.getContactPerson()).isEqualTo(updated.getContactPerson());
        assertThat(queried.getContactEmail()).isEqualTo(updated.getContactEmail());

        assertThat(update.getTitle()).isEqualTo(updated.getTitle());
        assertThat(inserted.getStudyId()).isEqualTo(updated.getStudyId());
        assertThat(inserted.getCreated()).isEqualTo(updated.getCreated());
        assertThat(inserted.getModified().toEpochMilli()).isLessThan(updated.getModified().toEpochMilli());
        assertThat(inserted.getContactPerson()).isNotEqualTo(updated.getContactPerson());
        assertThat(inserted.getContactEmail()).isNotEqualTo(updated.getContactEmail());
    }

    @Test
    @DisplayName("Studies are deleted and listed correctly")
    void testListAndDelete() {
        Study s1 = studyRepository.insert(new Study().setContactPerson("test person").setContactEmail("test mail"));
        Study s2 = studyRepository.insert(new Study().setContactPerson("test person").setContactEmail("test mail"));
        Study s3 = studyRepository.insert(new Study().setContactPerson("test person").setContactEmail("test mail"));

        assertThat(studyRepository.listStudyOrderByModifiedDesc()).hasSize(3);
        studyRepository.deleteById(s1.getStudyId());
        assertThat(studyRepository.listStudyOrderByModifiedDesc()).hasSize(2);
        studyRepository.deleteById(s2.getStudyId());
        assertThat(studyRepository.listStudyOrderByModifiedDesc()).hasSize(1);
        studyRepository.deleteById(s2.getStudyId());
        assertThat(studyRepository.listStudyOrderByModifiedDesc()).hasSize(1);
        studyRepository.deleteById(s3.getStudyId());
        assertThat(studyRepository.listStudyOrderByModifiedDesc()).isEmpty();
    }

    @Test
    @DisplayName("Study states are set correctly")
    void testSetState() {
        Study study = studyRepository.insert(new Study().setContactPerson("test person").setContactEmail("test mail"));
        assertThat(study.getStudyState()).isEqualTo(Study.Status.DRAFT);
        assertThat(study.getStartDate()).isNull();

        studyRepository.setStateById(study.getStudyId(), Study.Status.ACTIVE);

        study = assertPresent(studyRepository.getById(study.getStudyId()));
        assertThat(study.getStudyState()).isEqualTo(Study.Status.ACTIVE);
        assertThat(study.getStartDate()).isNotNull();
        assertThat(study.getEndDate()).isNull();

        studyRepository.setStateById(study.getStudyId(), Study.Status.PAUSED);
        study = assertPresent(studyRepository.getById(study.getStudyId()));
        assertThat(study.getStudyState()).isEqualTo(Study.Status.PAUSED);
        assertThat(study.getStartDate()).isNotNull();
        assertThat(study.getEndDate()).isNull();

        studyRepository.setStateById(study.getStudyId(), Study.Status.CLOSED);
        study = assertPresent(studyRepository.getById(study.getStudyId()));
        assertThat(study.getStudyState()).isEqualTo(Study.Status.CLOSED);
        assertThat(study.getStartDate()).isNotNull();
        assertThat(study.getEndDate()).isNotNull();
    }

    @Test
    @DisplayName("Study state correctly asserted")
    void testAssertState(){
        Set<Study.Status> statusSet1 = Set.of(Study.Status.ACTIVE, Study.Status.DRAFT);
        Set<Study.Status> statusSet2 = Set.of(Study.Status.CLOSED);
        Set<Study.Status> statusSet3 = Collections.emptySet();

        Study study = studyRepository.insert(new Study().setContactPerson("test person").setContactEmail("test mail"));
        assertTrue(studyRepository.hasState(study.getStudyId(), statusSet1));
        assertFalse(studyRepository.hasState(study.getStudyId(), statusSet2));
        assertFalse(studyRepository.hasState(study.getStudyId(), statusSet3));
    }


    private <T> T assertPresent(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<T> t) {
        assertThat(t).isPresent();
        return t.get();
    }
    private <T> T assertPresent(Supplier<Optional<T>> supplier) {
        return assertPresent(supplier.get());
    }

}
