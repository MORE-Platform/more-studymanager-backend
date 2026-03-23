/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.model.LoginToken;
import io.redlink.more.studymanager.model.LoginTokenApplication;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.ParticipantApplication;
import io.redlink.more.studymanager.model.ParticipantApplicationAccess;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.properties.ApplicationProperties;
import io.redlink.more.studymanager.repository.ParticipantApplicationRepository;
import io.redlink.more.studymanager.repository.StudyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class ApplicationAccessService {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationAccessService.class);
    private final LoginTokenService loginTokenService;
    private final ParticipantApplicationRepository participantApplicationRepository;
    private final StudyRepository studyRepository;
    private final ApplicationProperties applicationProperties;

    public ApplicationAccessService(LoginTokenService loginTokenService, ParticipantApplicationRepository participantApplicationRepository, StudyRepository studyRepository, ApplicationProperties applicationProperties) {
        this.loginTokenService = loginTokenService;
        this.participantApplicationRepository = participantApplicationRepository;
        this.studyRepository = studyRepository;
        this.applicationProperties = applicationProperties;
    }

    private boolean isValidApplication(String application) {
        return application != null && !application.isEmpty() && applicationProperties.getUrls().containsKey(application);
    }

    public Optional<ParticipantApplicationAccess> createMissingApplicationAccess(Long studyId, Integer participantId, String application) {
        Optional<Study> study = studyRepository.getById(studyId);
        if (study.isEmpty()) {
            throw new IllegalArgumentException("Study does not exist");
        }
        Set<String> studyApplications = study.get().getApplicationAccess();
        if (application == null || application.isEmpty() || !studyApplications.contains(application)) {
            throw new IllegalArgumentException("Application {stud}");
        }

        Optional<LoginToken> loginTokenOptional = loginTokenService.getToken(studyId, participantId, application);
        boolean tokenCreated = loginTokenOptional.isEmpty();
        LoginToken loginToken = loginTokenOptional
                .orElseGet(() -> loginTokenService.createToken(studyId, participantId, application));

        Optional<ParticipantApplication> participantApplicationOptional = participantApplicationRepository.findByIds(studyId, participantId, application);
        boolean participantApplicationCreated = participantApplicationOptional.isEmpty();
        ParticipantApplication participantApplication = participantApplicationOptional
                .orElseGet(() -> participantApplicationRepository.save(new ParticipantApplication()
                        .setStudyId(studyId)
                        .setParticipantId(participantId)
                        .setApplication(application)
                        .setUuid(UUID.randomUUID())));

        String baseUrl = applicationProperties.getUrls().get(application);
        return Optional.of(new ParticipantApplicationAccess()
                .setNewlyCreated(tokenCreated || participantApplicationCreated)
                .setApplicationType(application)
                .setAccessCode(loginToken.getCode())
                .setApplicationUrl(UriComponentsBuilder.fromUriString(baseUrl)
                        .pathSegment("{studyId}", "{uuid}")
                        .build(Map.of("studyId", studyId, "uuid", participantApplication.getUuid()))
                        .toString()));
    }

    public void updateApplicationAccess(Long studyId, Integer participantId, Collection<String> applications) {
        if (applications != null) {
            deleteApplicationAccess(studyId, participantId);
            applications.stream()
                    .filter(this::isValidApplication)
                    .forEach(application -> createMissingApplicationAccess(studyId, participantId, application));
        }
    }

    public void deleteApplicationAccess(Long studyId, Integer participantId, String application) {
        loginTokenService.deleteToken(studyId, participantId, application);
        participantApplicationRepository.delete(studyId, participantId, application);
    }

    public void deleteApplicationAccess(Long studyId, Integer participantId) {
        loginTokenService.deleteParticipantTokens(studyId, participantId);
        participantApplicationRepository.deleteAllByParticipant(studyId, participantId);
    }

    public void deleteApplicationAccess(Long studyId) {
        loginTokenService.deleteStudyTokens(studyId);
        participantApplicationRepository.deleteAllByStudy(studyId);
    }

    public void deleteApplicationAccessExcept(Long studyId, Set<String> applications) {
        loginTokenService.deleteTokensExcept(studyId, applications);
        participantApplicationRepository.deleteAllByStudyExcept(studyId, applications);
    }

    public List<ParticipantApplicationAccess> getParticipantApplicationAccess(Long studyId, Integer participantId) {
        return participantApplicationRepository.findAllByParticipant(studyId, participantId).stream()
                .filter(pa -> applicationProperties.getUrls().containsKey(pa.getApplication()))
                .map(pa -> {
                    String code = loginTokenService.getToken(studyId, participantId, pa.getApplication())
                            .map(LoginToken::getCode)
                            .orElse(null);
                    String baseUrl = applicationProperties.getUrls().get(pa.getApplication());
                    return new ParticipantApplicationAccess()
                            .setApplicationType(pa.getApplication())
                            .setAccessCode(code)
                            .setApplicationUrl(UriComponentsBuilder.fromUriString(baseUrl)
                                    .pathSegment("{studyId}", "{uuid}")
                                    .build(Map.of("studyId", studyId, "uuid", pa.getUuid()))
                                    .toString());
                })
                .toList();
    }

    public URI generateSignupUrl(Participant participant) {
        if (participant.getRegistrationToken() == null) return null;
        String baseUrl = applicationProperties.getUrls().get(LoginTokenApplication.PARTICIPANT_PORTAL.name());
        if (baseUrl == null) return null;

        return UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment("api", "v1", "signup")
                .queryParam("token", "{token}")
                .build(Map.of("token", participant.getRegistrationToken()));
    }

}
