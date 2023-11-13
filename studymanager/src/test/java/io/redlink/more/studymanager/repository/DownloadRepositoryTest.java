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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test-containers-flyway")
public class DownloadRepositoryTest {

    @Autowired
    private DownloadTokenRepository tokenRepository;

    @BeforeEach
    void deleteAll() {
        tokenRepository.clear();
    }

    @Test
    void testCreateAndSingleRead() {
        var token = tokenRepository.createToken(5L);
        assertNotNull(token);

        var returnToken = tokenRepository.getToken(token.getToken()).get();
        assertEquals(token.getToken(), returnToken.getToken());
        assertEquals(5L, returnToken.getStudyId());

        assertFalse(tokenRepository.getToken(token.getToken()).isPresent());
    }
}
