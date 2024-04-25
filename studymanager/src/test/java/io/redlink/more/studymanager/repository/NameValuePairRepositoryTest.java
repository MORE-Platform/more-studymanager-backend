/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.Contact;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.Study;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.Serializable;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test-containers-flyway")
public class NameValuePairRepositoryTest {

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private ObservationRepository observationRepository;

    @Autowired
    private NameValuePairRepository nvpairs;

    @BeforeEach
    void before() {
        studyRepository.clear();
        //Test cascade deletion
        assertTrue(nvpairs.noObservationValues());
    }

    @Test
    void testCrud() {
        long sid = studyRepository.insert(new Study().setContact(new Contact())).getStudyId();
        int oid1 = observationRepository.insert(new Observation().setStudyId(sid).setType("t").setHidden(false)).getObservationId();
        int oid2 = observationRepository.insert(new Observation().setStudyId(sid).setType("t").setHidden(false)).getObservationId();

        nvpairs.setObservationValue(sid, oid1, "n1", "v1");
        assertThat(nvpairs.getObservationValue(sid, oid1, "n1", String.class).get()).isEqualTo("v1");
        assertFalse(nvpairs.getObservationValue(sid, oid1, "n2", String.class).isPresent());
        assertFalse(nvpairs.getObservationValue(sid, oid2, "n1", String.class).isPresent());
        assertThrows(ClassCastException.class, () -> {
            nvpairs.getObservationValue(sid, oid1, "n1", Integer.class);
        });
        nvpairs.removeObservationValue(sid, oid1, "n1");
        assertFalse(nvpairs.getObservationValue(sid, oid1, "n1", String.class).isPresent());
    }

    @Test
    public void testMoreComplexObject() {
        long sid1 = studyRepository.insert(new Study().setContact(new Contact())).getStudyId();
        int oid1 = observationRepository.insert(new Observation().setStudyId(sid1).setType("t").setHidden(false)).getObservationId();
        nvpairs.setObservationValue(sid1, oid1, "complex", new SampleObject("v1"));
        assertThat(nvpairs.getObservationValue(sid1, 1, "complex", SampleObject.class).get().getValue()).isEqualTo("v1");
    }

}
