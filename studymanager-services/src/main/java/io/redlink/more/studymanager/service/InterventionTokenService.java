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
import io.redlink.more.studymanager.repository.InterventionTokenRepository;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class InterventionTokenService {

    private final StudyStateService studyStateService;
    private final InterventionTokenRepository repository;
    private final PasswordEncoder passwordEncoder;

    public InterventionTokenService(StudyStateService studyStateService,
                                    InterventionTokenRepository repository,
                                    PasswordEncoder passwordEncoder) {
        this.studyStateService = studyStateService;
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<EndpointToken> addToken(Long studyId, Integer interventionId, String tokenLabel) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        String secret = UUID.randomUUID().toString();

        Optional<EndpointToken> newToken = repository.addToken(studyId, interventionId,
                tokenLabel,
                passwordEncoder.encode(secret)
        );

        return newToken.map(token ->
                token.withToken(
                        String.format("%s.%s",
                                Base64.getEncoder().encodeToString(
                                        String.format("%s-%s-%s", studyId, interventionId, token.tokenId()).getBytes(StandardCharsets.UTF_8)),
                                Base64.getEncoder().encodeToString(
                                        secret.getBytes(StandardCharsets.UTF_8))
                        )
                )
        );
    }

    public List<EndpointToken> getTokens(Long studyId, Integer interventionId) {
        return repository.getAllTokens(studyId, interventionId);
    }

    public Optional<EndpointToken> getToken(Long studyId, Integer interventionId, Integer tokenId) {
        return repository.getToken(studyId, interventionId, tokenId);
    }

    public void deleteToken(Long studyId, Integer interventionId, Integer tokenId) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        repository.deleteToken(studyId, interventionId, tokenId);
    }

    public void alignWithStudyState(Study study) {
        if (study.getStudyState() == Study.Status.CLOSED) {
            repository.clearForStudyId(study.getStudyId());
        }
    }

    /**
     * Validates an API token and returns the resolved study and intervention IDs.
     *
     * @param moreApiToken the token in format Base64(studyId-interventionId-tokenId).Base64(secret)
     * @return resolved token info with studyId and interventionId
     * @throws AccessDeniedException if the token is invalid
     */
    public ResolvedToken validateToken(String moreApiToken) {
        try {
            String[] split = moreApiToken.split("\\.");
            if (split.length != 2) {
                throw new AccessDeniedException("Invalid token format");
            }

            String[] primaryKey = new String(
                    Base64.getDecoder().decode(split[0]), StandardCharsets.UTF_8
            ).split("-");
            if (primaryKey.length != 3) {
                throw new AccessDeniedException("Invalid token format");
            }

            Long studyId = Long.valueOf(primaryKey[0]);
            Integer interventionId = Integer.valueOf(primaryKey[1]);
            Integer tokenId = Integer.valueOf(primaryKey[2]);

            String secret = new String(
                    Base64.getDecoder().decode(split[1]), StandardCharsets.UTF_8
            );

            Optional<String> storedHash = repository.getTokenSecret(studyId, interventionId, tokenId);
            if (storedHash.isEmpty() || !passwordEncoder.matches(secret, storedHash.get())) {
                throw new AccessDeniedException("Invalid token");
            }

            return new ResolvedToken(studyId, interventionId);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new AccessDeniedException("Invalid token");
        }
    }

    public record ResolvedToken(Long studyId, Integer interventionId) {}
}
