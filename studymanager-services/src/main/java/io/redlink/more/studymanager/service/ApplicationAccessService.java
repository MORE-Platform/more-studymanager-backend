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
import io.redlink.more.studymanager.properties.ApplicationProperties;
import io.redlink.more.studymanager.repository.ParticipantApplicationRepository;
import io.redlink.more.studymanager.repository.ParticipantRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ApplicationAccessService {

    private final LoginTokenService loginTokenService;
    private final ParticipantApplicationRepository participantApplicationRepository;
    private final ParticipantRepository participantRepository;
    private final ApplicationProperties applicationProperties;

    public ApplicationAccessService(LoginTokenService loginTokenService, ParticipantApplicationRepository participantApplicationRepository, ParticipantRepository participantRepository, ApplicationProperties applicationProperties) {
        this.loginTokenService = loginTokenService;
        this.participantApplicationRepository = participantApplicationRepository;
        this.participantRepository = participantRepository;
        this.applicationProperties = applicationProperties;
    }

    public void generateApplicationAccess(Long studyId, Integer participantId, Collection<String> applications) {
        if (applications != null && !applications.isEmpty()) {
            applications.stream()
                    .filter(this::isValidApplication)
                    .filter(application -> applicationProperties.getUrls().containsKey(application))
                    .forEach(application -> {
                        loginTokenService.getToken(studyId, participantId, application)
                                .orElseGet(() -> loginTokenService.createToken(studyId, participantId, application));
                        participantApplicationRepository.findByIds(studyId, participantId, application)
                                .orElseGet(() -> participantApplicationRepository.save(new ParticipantApplication()
                                        .setStudyId(studyId)
                                        .setParticipantId(participantId)
                                        .setApplication(application)
                                        .setUuid(UUID.randomUUID())));
                    });
        }
    }

    public void generateMissingApplicationAccess(Long studyId, Collection<String> applications) {
        if (applications != null && !applications.isEmpty()) {
            List<Participant> participants = participantRepository.listParticipants(studyId);
            applications.stream()
                    .filter(this::isValidApplication)
                    .filter(application -> applicationProperties.getUrls().containsKey(application))
                    .forEach(application -> {
                        loginTokenService.createMissingTokens(studyId, application);
                        List<ParticipantApplication> existingMappings = participantApplicationRepository.findAllByStudyAndApplication(studyId, application);
                        Set<Integer> participantsWithMappings = existingMappings.stream()
                                .map(ParticipantApplication::getParticipantId)
                                .collect(Collectors.toSet());

                        participants.stream()
                                .filter(p -> !participantsWithMappings.contains(p.getParticipantId()))
                                .forEach(p -> participantApplicationRepository.save(new ParticipantApplication()
                                        .setStudyId(studyId)
                                        .setParticipantId(p.getParticipantId())
                                        .setApplication(application)
                                        .setUuid(UUID.randomUUID())));
                    });
        }
    }

    private boolean isValidApplication(String application) {
        try {
            LoginTokenApplication.valueOf(application);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void updateApplicationAccess(Long studyId, Integer participantId, Collection<String> applications) {
        if (applications != null) {
            deleteApplicationAccess(studyId, participantId);
            generateApplicationAccess(studyId, participantId, applications);
        }
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
