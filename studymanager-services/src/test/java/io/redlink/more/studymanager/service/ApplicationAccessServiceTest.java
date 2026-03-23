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
import io.redlink.more.studymanager.repository.ParticipantRepository;
import io.redlink.more.studymanager.repository.StudyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationAccessServiceTest {

    @Mock
    private LoginTokenService loginTokenService;
    @Mock
    private ParticipantApplicationRepository participantApplicationRepository;
    @Mock
    private ParticipantRepository participantRepository;
    @Mock
    private StudyRepository studyRepository;
    @Mock
    private ApplicationProperties applicationProperties;

    @InjectMocks
    private ApplicationAccessService applicationAccessService;

    private final Long studyId = 1L;
    private final Integer participantId = 10;
    private final String application = LoginTokenApplication.PARTICIPANT_PORTAL.name();
    private final String baseUrl = "https://example.com";

    @BeforeEach
    void setUp() {
        Map<String, String> urls = new HashMap<>();
        urls.put(application, baseUrl);
        lenient().when(applicationProperties.getUrls()).thenReturn(urls);
    }

    @Test
    @DisplayName("createMissingApplicationAccess should create tokens and applications if they don't exist")
    void testCreateMissingApplicationAccess() {
        Study study = new Study().setApplicationAccess(Set.of(application));
        when(studyRepository.getById(studyId)).thenReturn(Optional.of(study));
        when(loginTokenService.getToken(anyLong(), anyInt(), anyString())).thenReturn(Optional.empty());
        when(loginTokenService.createToken(anyLong(), anyInt(), anyString())).thenReturn(new LoginToken().setCode("code"));
        when(participantApplicationRepository.findByIds(anyLong(), anyInt(), anyString())).thenReturn(Optional.empty());
        when(participantApplicationRepository.save(any(ParticipantApplication.class))).thenReturn(new ParticipantApplication().setUuid(UUID.randomUUID()));

        Optional<ParticipantApplicationAccess> result = applicationAccessService.createMissingApplicationAccess(studyId, participantId, application);

        assertThat(result).isPresent();
        verify(loginTokenService).createToken(studyId, participantId, application);
        verify(participantApplicationRepository).save(any(ParticipantApplication.class));
    }

    @Test
    @DisplayName("createMissingApplicationAccess should not create tokens and applications if they exist")
    void testCreateMissingApplicationAccessExisting() {
        Study study = new Study().setApplicationAccess(Set.of(application));
        when(studyRepository.getById(studyId)).thenReturn(Optional.of(study));
        when(loginTokenService.getToken(anyLong(), anyInt(), anyString())).thenReturn(Optional.of(new LoginToken().setCode("code")));
        when(participantApplicationRepository.findByIds(anyLong(), anyInt(), anyString())).thenReturn(Optional.of(new ParticipantApplication().setUuid(UUID.randomUUID())));

        Optional<ParticipantApplicationAccess> result = applicationAccessService.createMissingApplicationAccess(studyId, participantId, application);

        assertThat(result).isPresent();
        verify(loginTokenService, never()).createToken(anyLong(), anyInt(), anyString());
        verify(participantApplicationRepository, never()).save(any(ParticipantApplication.class));
    }

    @Test
    @DisplayName("updateApplicationAccess should do nothing if applications is null")
    void testUpdateApplicationAccessNull() {
        applicationAccessService.updateApplicationAccess(studyId, participantId, null);

        verify(loginTokenService, never()).deleteParticipantTokens(anyLong(), anyInt());
    }

    @Test
    @DisplayName("updateApplicationAccess should delete existing and generate new access")
    void testUpdateApplicationAccess() {
        Study study = new Study().setApplicationAccess(Set.of(application));
        when(studyRepository.getById(studyId)).thenReturn(Optional.of(study));
        when(loginTokenService.getToken(anyLong(), anyInt(), anyString())).thenReturn(Optional.empty());
        when(loginTokenService.createToken(anyLong(), anyInt(), anyString())).thenReturn(new LoginToken().setCode("code"));
        when(participantApplicationRepository.findByIds(anyLong(), anyInt(), anyString())).thenReturn(Optional.empty());
        when(participantApplicationRepository.save(any(ParticipantApplication.class))).thenReturn(new ParticipantApplication().setUuid(UUID.randomUUID()));

        applicationAccessService.updateApplicationAccess(studyId, participantId, Set.of(application));

        verify(loginTokenService).deleteParticipantTokens(studyId, participantId);
        verify(participantApplicationRepository).deleteAllByParticipant(studyId, participantId);
        verify(loginTokenService).getToken(studyId, participantId, application);
    }

    @Test
    @DisplayName("deleteApplicationAccess (study, participant) should call correct service methods")
    void testDeleteApplicationAccessParticipant() {
        applicationAccessService.deleteApplicationAccess(studyId, participantId);

        verify(loginTokenService).deleteParticipantTokens(studyId, participantId);
        verify(participantApplicationRepository).deleteAllByParticipant(studyId, participantId);
    }

    @Test
    @DisplayName("deleteApplicationAccess (study) should call correct service methods")
    void testDeleteApplicationAccessStudy() {
        applicationAccessService.deleteApplicationAccess(studyId);

        verify(loginTokenService).deleteStudyTokens(studyId);
        verify(participantApplicationRepository).deleteAllByStudy(studyId);
    }

    @Test
    @DisplayName("deleteApplicationAccessExcept should call correct service methods")
    void testDeleteApplicationAccessExcept() {
        Set<String> applications = Set.of(application);
        applicationAccessService.deleteApplicationAccessExcept(studyId, applications);

        verify(loginTokenService).deleteTokensExcept(studyId, applications);
        verify(participantApplicationRepository).deleteAllByStudyExcept(studyId, applications);
    }

    @Test
    @DisplayName("getParticipantApplicationAccess should return list of application access info")
    void testGetParticipantApplicationAccess() {
        UUID uuid = UUID.randomUUID();
        ParticipantApplication pa = new ParticipantApplication()
                .setApplication(application)
                .setUuid(uuid);
        when(participantApplicationRepository.findAllByParticipant(studyId, participantId)).thenReturn(List.of(pa));
        LoginToken token = new LoginToken().setCode("secret-code");
        when(loginTokenService.getToken(studyId, participantId, application)).thenReturn(Optional.of(token));

        List<ParticipantApplicationAccess> result = applicationAccessService.getParticipantApplicationAccess(studyId, participantId);

        assertThat(result).hasSize(1);
        ParticipantApplicationAccess access = result.get(0);
        assertThat(access.getApplicationType()).isEqualTo(application);
        assertThat(access.getAccessCode()).isEqualTo("secret-code");
        assertThat(access.getApplicationUrl()).contains(uuid.toString());
        assertThat(access.getApplicationUrl()).startsWith(baseUrl);
    }

    @Test
    @DisplayName("generateSignupUrl should return correct URI for participant")
    void testGenerateSignupUrl() {
        Participant participant = new Participant().setRegistrationToken("reg-token");

        URI result = applicationAccessService.generateSignupUrl(participant);

        assertThat(result).isNotNull();
        assertThat(result.toString()).contains("reg-token");
        assertThat(result.toString()).startsWith(baseUrl);
    }

    @Test
    @DisplayName("generateSignupUrl should return null if participant has no registration token")
    void testGenerateSignupUrlNoToken() {
        Participant participant = new Participant();

        URI result = applicationAccessService.generateSignupUrl(participant);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("generateSignupUrl should return null if base url is not configured")
    void testGenerateSignupUrlNoBaseUrl() {
        when(applicationProperties.getUrls()).thenReturn(Collections.emptyMap());
        Participant participant = new Participant().setRegistrationToken("token");

        URI result = applicationAccessService.generateSignupUrl(participant);

        assertThat(result).isNull();
    }
}
