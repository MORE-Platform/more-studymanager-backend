/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.event.StudyStateChangedEvent;
import io.redlink.more.studymanager.model.LoginToken;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.ParticipantApplication;
import io.redlink.more.studymanager.model.ParticipantApplicationAccess;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.properties.ApplicationProperties;
import io.redlink.more.studymanager.repository.ParticipantApplicationRepository;
import io.redlink.more.studymanager.repository.ParticipantRepository;
import io.redlink.more.studymanager.repository.StudyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

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
    private final ParticipantRepository participantRepository;
    private final StudyStateService studyStateService;
    private final StudyRepository studyRepository;
    private final ApplicationProperties applicationProperties;

    public ApplicationAccessService(LoginTokenService loginTokenService, ParticipantApplicationRepository participantApplicationRepository, ParticipantRepository participantRepository, StudyStateService studyStateService, StudyRepository studyRepository, ApplicationProperties applicationProperties) {
        this.loginTokenService = loginTokenService;
        this.participantApplicationRepository = participantApplicationRepository;
        this.participantRepository = participantRepository;
        this.studyStateService = studyStateService;
        this.studyRepository = studyRepository;
        this.applicationProperties = applicationProperties;
    }

    private boolean isValidApplication(String application) {
        return application != null && !application.isEmpty() && applicationProperties.getUrls().containsKey(application);
    }

    public Optional<ParticipantApplicationAccess> createApplicationAccess(Long studyId, Integer participantId, String application) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        var accessData = createMissingApplicationAccess(studyId, participantId, application);
        if (accessData.isPresent()) {
            participantRepository.setStatusIfCurrentStatusIs(studyId, participantId, Participant.Status.INVITED, Participant.Status.NEW);
        }
        return accessData;
    }

    private Optional<ParticipantApplicationAccess> createMissingApplicationAccess(Long studyId, Integer participantId, String application) {
        Optional<Study> study = studyRepository.getById(studyId);
        if (study.isEmpty()) {
            throw new IllegalArgumentException("Study does not exist");
        }
        Set<String> studyApplications = study.get().getApplicationAccess();
        if (application == null || application.isEmpty() || !studyApplications.contains(application)) {
            LOG.warn("Application {} does not exist", application);
            return Optional.empty();
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

    public Optional<ParticipantApplicationAccess> getParticipantApplicationAccess(Long studyId, Integer participantId, String application) {
        if (studyId == null || participantId == null || application == null || application.isEmpty() || !applicationProperties.getUrls().containsKey(application)) {
            throw new IllegalArgumentException("Provided arguments are invalid!");
        }
        Optional<ParticipantApplication> applicationAccess = participantApplicationRepository.findByIds(studyId, participantId, application);
        if (applicationAccess.isEmpty()) {
            return Optional.empty();
        }
        return getCredentialsForApplication(studyId, participantId, applicationAccess.get());
    }

    public List<ParticipantApplicationAccess> getParticipantApplicationAccess(Long studyId, Integer participantId) {
        return participantApplicationRepository.findAllByParticipant(studyId, participantId).stream()
                .filter(pa -> applicationProperties.getUrls().containsKey(pa.getApplication()))
                .map(pa -> getCredentialsForApplication(studyId, participantId, pa))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Optional<ParticipantApplicationAccess> getCredentialsForApplication(Long studyId, Integer participantId, ParticipantApplication application) {
        if (!applicationProperties.getUrls().containsKey(application.getApplication())) {
            return Optional.empty();
        }
        Optional<String> code = loginTokenService.getToken(studyId, participantId, application.getApplication())
                .map(LoginToken::getCode);
        if (code.isEmpty()) {
            return Optional.empty();
        }
        String baseUrl = applicationProperties.getUrls().get(application.getApplication());
        return Optional.ofNullable(new ParticipantApplicationAccess()
                .setApplicationType(application.getApplication())
                .setAccessCode(code.get())
                .setApplicationUrl(UriComponentsBuilder.fromUriString(baseUrl)
                        .pathSegment("{studyId}", "{uuid}")
                        .build(Map.of("studyId", studyId, "uuid", application.getUuid()))
                        .toString()));
    }

    public boolean deleteParticipantApplicationAccess(Long studyId, Integer participantId, String application) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        deleteApplicationAccess(studyId, participantId, application);
        participantRepository.setStatusIfCurrentStatusIs(studyId, participantId, Participant.Status.NEW, Participant.Status.INVITED);
        return true;
    }

    @EventListener
    @Transactional
    public void handleStudyStateChange(StudyStateChangedEvent event) {
        alignApplicationAccessWithStudyState(event.getStudy());
    }

    private void alignApplicationAccessWithStudyState(Study study) {
        switch (study.getStudyState()) {
            case DRAFT, CLOSED -> deleteApplicationAccess(study.getStudyId());
            case ACTIVE, PREVIEW -> deleteApplicationAccessExcept(study.getStudyId(), study.getApplicationAccess());
        }
    }
}
