/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.model.EndpointToken;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.repository.IntegrationRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class IntegrationService {

    private final StudyStateService studyStateService;
    private final IntegrationRepository repository;
    private final PasswordEncoder passwordEncoder;

    public IntegrationService(StudyStateService studyStateService, IntegrationRepository repository,
                              PasswordEncoder passwordEncoder) {
        this.studyStateService = studyStateService;
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<EndpointToken> addToken(Long studyId, Integer observationId, String tokenLabel) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        String secret = UUID.randomUUID().toString();

        Optional<EndpointToken> newToken = repository.addToken(studyId, observationId,
                tokenLabel,
                passwordEncoder.encode(secret)
        );

        return newToken.map(token ->
                token.withToken(
                        String.format("%s.%s",
                                Base64.getEncoder().encodeToString(
                                        String.format("%s-%s-%s", studyId, observationId, token.tokenId()).getBytes(StandardCharsets.UTF_8)),
                                Base64.getEncoder().encodeToString(
                                        secret.getBytes(StandardCharsets.UTF_8))
                        )
                )
        );
    }

    public Optional<EndpointToken> getToken(Long studyId, Integer observationId, Integer tokenId) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        return repository.getToken(studyId, observationId, tokenId);
    }

    public List<EndpointToken> getTokens(Long studyId, Integer observationId) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        return repository.getAllTokens(studyId, observationId);
    }

    public void deleteToken(Long studyId, Integer observationId, Integer tokenId) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        repository.deleteToken(studyId, observationId, tokenId);
    }

    public void alignIntegrationsWithStudyState(Study study) {
        if(study.getStudyState() == Study.Status.CLOSED) {
            repository.clearForStudyId(study.getStudyId());
        }
    }
}
