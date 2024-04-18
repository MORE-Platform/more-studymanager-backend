/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.Serializable;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test-containers-flyway")
public class NameValuePairRepositoryTest {

    @Autowired
    private NameValuePairRepository nvpairs;

    @BeforeEach
    void deleteAll() {
        nvpairs.clear();
    }

    @Test
    public void testCrud() {
        nvpairs.setValue("i1", "n1", "v1");
        assertThat(nvpairs.getValue("i1", "n1", String.class).get()).isEqualTo("v1");
        assertFalse(nvpairs.getValue("i1", "n2", String.class).isPresent());
        assertFalse(nvpairs.getValue("i2", "n1", String.class).isPresent());
        assertThrows(ClassCastException.class, () -> {
            nvpairs.getValue("i1", "n1", Integer.class);
        });
        nvpairs.removeValue("i1", "n1");
        assertFalse(nvpairs.getValue("i1", "n1", String.class).isPresent());
    }

    @Test
    public void testMoreComplexObject() {
        nvpairs.setValue("i2", "complex", new SampleObject("v1"));
        assertThat(nvpairs.getValue("i2", "complex", SampleObject.class).get().getValue()).isEqualTo("v1");
    }

}
