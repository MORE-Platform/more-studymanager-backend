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
import io.redlink.more.studymanager.model.SaltToken;
import io.redlink.more.studymanager.properties.LoginTokenProperties;
import io.redlink.more.studymanager.repository.LoginTokenRepository;
import io.redlink.more.studymanager.repository.SaltTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginTokenServiceTest {

    @Mock
    private LoginTokenRepository loginTokenRepository;

    @Mock
    private SaltTokenRepository saltTokenRepository;

    private LoginTokenProperties properties;
    private LoginTokenService loginTokenService;

    private final Long studyId = 1L;
    private final Integer participantId = 100;
    private final String application = "test-app";

    @BeforeEach
    void setUp() {
        properties = new LoginTokenProperties();
        properties.setEncryptionKey("265d299d1ed0ce45e8ff115ead0bf8a6de91399d3880a5f2d70870fd828ca3ec");
        properties.setSaltKey("2be395cf978fff579da4b595116e190fff6ad898e1344faff2638012fa373534");
        properties.setHashAlgorithm("SHA-256");
        properties.setLength(8);
        properties.setUseLetters(true);
        properties.setUseNumbers(true);

        loginTokenService = new LoginTokenService(loginTokenRepository, saltTokenRepository, properties);
    }

    @Test
    @DisplayName("validateConfiguration should throw exception if encryption key is default or missing")
    void testValidateConfigurationEncryptionKey() {
        properties.setEncryptionKey("");
        assertThatThrownBy(() -> properties.validateConfiguration())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("encryption key");

        properties.setEncryptionKey(null);
        assertThatThrownBy(() -> properties.validateConfiguration())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("encryption key");
    }

    @Test
    @DisplayName("validateConfiguration should throw exception if salt key is default or missing")
    void testValidateConfigurationSaltKey() {
        properties.setSaltKey("");
        assertThatThrownBy(() -> properties.validateConfiguration())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("salt key");

        properties.setSaltKey(null);
        assertThatThrownBy(() -> properties.validateConfiguration())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("salt key");
    }


    @Test
    @DisplayName("validateConfiguration should throw exception if token length is null or less than 4")
    void testValidateConfigurationTokenLength() {
        properties.setLength(null);
        assertThatThrownBy(() -> properties.validateConfiguration())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Login token length must be greater than or equal to 4");
        properties.setLength(3);
        assertThatThrownBy(() -> properties.validateConfiguration())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Login token length must be greater than or equal to 4");
    }

    @Test
    @DisplayName("validateConfiguration should throw exception for invalid hash algorithm")
    void testValidateConfigurationHashAlgorithm() {
        properties.setHashAlgorithm("INVALID-HASH");
        assertThatThrownBy(() -> properties.validateConfiguration())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid hash algorithm");
    }

    @Test
    @DisplayName("createToken should generate, encrypt and save a new token and salt")
    void testCreateToken() {
        when(saltTokenRepository.findByIds(studyId, participantId)).thenReturn(Optional.empty());

        LoginToken createdToken = loginTokenService.createToken(studyId, participantId, application);

        assertThat(createdToken).isNotNull();
        assertThat(createdToken.getStudyId()).isEqualTo(studyId);
        assertThat(createdToken.getParticipantId()).isEqualTo(participantId);
        assertThat(createdToken.getApplication()).isEqualTo(application);
        assertThat(createdToken.getCode()).hasSize(properties.getLength());
        assertThat(createdToken.getCodeHash()).isNotNull();

        verify(saltTokenRepository).save(any(SaltToken.class));
        verify(loginTokenRepository).save(any(LoginToken.class));
    }

    @Test
    @DisplayName("createToken should use existing salt if available")
    void testCreateTokenWithExistingSalt() {

        when(saltTokenRepository.findByIds(eq(studyId), eq(participantId))).thenReturn(Optional.empty());
        ArgumentCaptor<SaltToken> saltCaptor = ArgumentCaptor.forClass(SaltToken.class);
        loginTokenService.createToken(studyId, participantId, application);
        verify(saltTokenRepository).save(saltCaptor.capture());
        SaltToken validSaltToken = saltCaptor.getValue();

        reset(saltTokenRepository, loginTokenRepository);

        when(saltTokenRepository.findByIds(studyId, participantId)).thenReturn(Optional.of(validSaltToken));

        LoginToken createdToken = loginTokenService.createToken(studyId, participantId, application);

        assertThat(createdToken).isNotNull();
        verify(saltTokenRepository, never()).save(any(SaltToken.class));
        verify(loginTokenRepository).save(any(LoginToken.class));
    }

    @Test
    @DisplayName("getToken should return Optional.empty() if token is not found")
    void testGetTokenNotFound() {
        when(loginTokenRepository.findByIds(studyId, participantId, application)).thenReturn(Optional.empty());

        Optional<LoginToken> result = loginTokenService.getToken(studyId, participantId, application);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getToken should throw exception if token is found but decryption fails (e.g. invalid salt format)")
    void testGetTokenDecryptionFailure() {
        LoginToken encryptedToken = new LoginToken()
                .setStudyId(studyId)
                .setParticipantId(participantId)
                .setApplication(application)
                .setCode("not-hex-at-all");

        when(loginTokenRepository.findByIds(studyId, participantId, application)).thenReturn(Optional.of(encryptedToken));
        // Mock a salt that will cause decryption to fail or a missing salt
        when(saltTokenRepository.findByIds(eq(studyId), eq(participantId))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginTokenService.getToken(studyId, participantId, application))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("getToken should return decrypted token correctly when salt exists")
    void testGetTokenSuccess() {
        ArgumentCaptor<SaltToken> saltCaptor = ArgumentCaptor.forClass(SaltToken.class);
        LoginToken[] savedEncryptedToken = new LoginToken[1];
        doAnswer(invocation -> {
            LoginToken tokenToSave = invocation.getArgument(0);
            savedEncryptedToken[0] = new LoginToken()
                    .setStudyId(tokenToSave.getStudyId())
                    .setParticipantId(tokenToSave.getParticipantId())
                    .setApplication(tokenToSave.getApplication())
                    .setCode(tokenToSave.getCode())
                    .setCodeHash(tokenToSave.getCodeHash());
            return tokenToSave;
        }).when(loginTokenRepository).save(any(LoginToken.class));

        when(saltTokenRepository.findByIds(eq(studyId), eq(participantId))).thenReturn(Optional.empty());

        LoginToken created = loginTokenService.createToken(studyId, participantId, application);
        String originalCode = created.getCode();

        verify(saltTokenRepository).save(saltCaptor.capture());
        verify(loginTokenRepository).save(any(LoginToken.class));

        SaltToken validSaltToken = saltCaptor.getValue();
        LoginToken validToken = savedEncryptedToken[0];
        assertThat(validToken).isNotNull();

        // Use a fresh instance of the service with a fresh TextEncryptor to be sure
        LoginTokenService freshService = new LoginTokenService(loginTokenRepository, saltTokenRepository, properties);

        // 2. Prepare for retrieval
        reset(saltTokenRepository, loginTokenRepository);
        when(saltTokenRepository.findByIds(studyId, participantId)).thenReturn(Optional.of(validSaltToken));
        when(loginTokenRepository.findByIds(studyId, participantId, application)).thenReturn(Optional.of(validToken));

        // 3. Retrieve and decrypt
        Optional<LoginToken> retrieved = freshService.getToken(studyId, participantId, application);

        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getCode()).isEqualTo(originalCode);
    }

    @Test
    @DisplayName("getAllTokens should throw exception if any token fails to decrypt")
    void testGetAllTokens() {
        LoginToken t1 = new LoginToken().setStudyId(studyId).setParticipantId(participantId).setApplication("app1").setCode("c1");

        when(loginTokenRepository.findAllByParticipant(studyId, participantId)).thenReturn(List.of(t1));
        when(saltTokenRepository.findByIds(studyId, participantId)).thenReturn(Optional.of(new SaltToken().setSalt("salt")));

        assertThatThrownBy(() -> loginTokenService.getAllTokens(studyId, participantId))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("updateToken should generate a new code")
    void testUpdateToken() {
        when(saltTokenRepository.findByIds(studyId, participantId)).thenReturn(Optional.empty());

        LoginToken updated = loginTokenService.updateToken(studyId, participantId, application);

        assertThat(updated).isNotNull();
        verify(loginTokenRepository).save(any(LoginToken.class));
    }

    @Test
    @DisplayName("deleteToken should call repository delete")
    void testDeleteToken() {
        loginTokenService.deleteToken(studyId, participantId, application);
        verify(loginTokenRepository).delete(studyId, participantId, application);
    }

    @Test
    @DisplayName("deleteParticipantTokens should call repository deleteAllByParticipant and saltRepository delete")
    void testDeleteParticipantTokens() {
        loginTokenService.deleteParticipantTokens(studyId, participantId);
        verify(loginTokenRepository).deleteAllByParticipant(studyId, participantId);
        verify(saltTokenRepository).delete(studyId, participantId);
    }

    @Test
    @DisplayName("deleteStudyTokens should call repository deleteAllByStudy and saltRepository deleteAllByStudy")
    void testDeleteStudyTokens() {
        loginTokenService.deleteStudyTokens(studyId);
        verify(loginTokenRepository).deleteAllByStudy(studyId);
        verify(saltTokenRepository).deleteAllByStudy(studyId);
    }

    @Test
    @DisplayName("deleteTokens should call repository deleteAllByStudyAndApplication")
    void testDeleteTokens() {
        loginTokenService.deleteTokens(studyId, application);
        verify(loginTokenRepository).deleteAllByStudyAndApplication(studyId, application);
    }

    @Test
    @DisplayName("Token generation should respect configuration")
    void testTokenGenerationConfig() {
        // Test letters only
        properties.setUseLetters(true);
        properties.setUseNumbers(false);
        properties.setLength(10);
        loginTokenService = new LoginTokenService(loginTokenRepository, saltTokenRepository, properties);
        // We need to return an empty salt so it creates a new one (properly encrypted)
        when(saltTokenRepository.findByIds(any(), any())).thenReturn(Optional.empty());

        LoginToken token = loginTokenService.createToken(studyId, participantId, application);
        assertThat(token.getCode()).hasSize(10);
        assertThat(token.getCode()).containsPattern("^[A-Z]+$");

        // Test numbers only
        properties.setUseLetters(false);
        properties.setUseNumbers(true);
        properties.setLength(5);
        loginTokenService = new LoginTokenService(loginTokenRepository, saltTokenRepository, properties);
        // Reset mock as it's a new service instance but sharing the mock
        reset(saltTokenRepository);
        when(saltTokenRepository.findByIds(any(), any())).thenReturn(Optional.empty());

        token = loginTokenService.createToken(studyId, participantId, application);
        assertThat(token.getCode()).hasSize(5);
        assertThat(token.getCode()).containsPattern("^[2-9]+$");
    }

    @Test
    @DisplayName("createMissingToken should only create token if it does not exist")
    void testCreateMissingToken() {
        when(loginTokenRepository.findAllByStudyAndApplication(studyId, application)).thenReturn(List.of());
        when(saltTokenRepository.findByIds(eq(studyId), eq(participantId))).thenReturn(Optional.empty());

        loginTokenService.createMissingToken(studyId, participantId, application);

        verify(loginTokenRepository, times(1)).save(any(LoginToken.class));

        LoginToken t1 = new LoginToken().setStudyId(studyId).setParticipantId(participantId).setApplication(application);
        when(loginTokenRepository.findAllByStudyAndApplication(studyId, application)).thenReturn(List.of(t1));

        loginTokenService.createMissingToken(studyId, participantId, application);
        // Should not have called save again
        verify(loginTokenRepository, times(1)).save(any(LoginToken.class));
    }
}
