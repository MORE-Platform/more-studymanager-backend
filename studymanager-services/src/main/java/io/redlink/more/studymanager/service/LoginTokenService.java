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
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class LoginTokenService {

    private static final Logger log = LoggerFactory.getLogger(LoginTokenService.class);

    private final LoginTokenRepository loginTokenRepository;
    private final SaltTokenRepository saltTokenRepository;
    private final LoginTokenProperties properties;
    private final TextEncryptor saltEncryptor;

    public LoginTokenService(LoginTokenRepository loginTokenRepository, SaltTokenRepository saltTokenRepository, LoginTokenProperties properties) {
        this.loginTokenRepository = loginTokenRepository;
        this.saltTokenRepository = saltTokenRepository;
        this.properties = properties;
        this.saltEncryptor = Encryptors.text(properties.getEncryptionKey(), hashToken(properties.getSaltKey()));
    }

    public LoginToken createToken(Long studyId, Integer participantId, String application) {
        String code = generateCode();
        String salt = getOrCreateSalt(studyId, participantId);
        TextEncryptor tokenEncryptor = Encryptors.text(properties.getEncryptionKey(), hashToken(salt));

        LoginToken token = new LoginToken()
                .setStudyId(studyId)
                .setParticipantId(participantId)
                .setApplication(application)
                .setCode(tokenEncryptor.encrypt(code))
                .setCodeHash(hashToken(code));

        loginTokenRepository.save(token);
        return token.setCode(code);
    }

    public Optional<LoginToken> getToken(Long studyId, Integer participantId, String application) {
        return loginTokenRepository.findByIds(studyId, participantId, application)
                .map(token -> decryptToken(token, studyId, participantId));
    }

    public List<LoginToken> getAllTokens(Long studyId, Integer participantId) {
        return loginTokenRepository.findAllByParticipant(studyId, participantId).stream()
                .map(token -> decryptToken(token, studyId, participantId))
                .toList();
    }

    public LoginToken createMissingToken(Long studyId, Integer participantId, String application) {
        List<LoginToken> existingTokens = loginTokenRepository.findAllByStudyAndApplication(studyId, application);
        Optional<LoginToken> existingApplicationToken = existingTokens.stream().filter(token -> token.getParticipantId().equals(participantId)).findFirst();
        return existingApplicationToken.orElseGet(() -> createToken(studyId, participantId, application));
    }

    public LoginToken updateToken(Long studyId, Integer participantId, String application) {
        return createToken(studyId, participantId, application);
    }

    public void deleteTokens(Long studyId, String application) {
        loginTokenRepository.deleteAllByStudyAndApplication(studyId, application);
    }

    public void deleteTokensExcept(Long studyId, Set<String> applications) {
        loginTokenRepository.deleteAllByStudyExcept(studyId, applications);
    }

    public void deleteToken(Long studyId, Integer participantId, String application) {
        loginTokenRepository.delete(studyId, participantId, application);
    }

    public void deleteParticipantTokens(Long studyId, Integer participantId) {
        loginTokenRepository.deleteAllByParticipant(studyId, participantId);
        saltTokenRepository.delete(studyId, participantId);
    }

    public void deleteStudyTokens(Long studyId) {
        loginTokenRepository.deleteAllByStudy(studyId);
        saltTokenRepository.deleteAllByStudy(studyId);
    }

    private String getOrCreateSalt(Long studyId, Integer participantId) {
        return saltTokenRepository.findByIds(studyId, participantId)
                .map(this::decryptSalt)
                .orElseGet(() -> {
                    String newSalt = KeyGenerators.string().generateKey();
                    saltTokenRepository.save(new SaltToken()
                            .setStudyId(studyId)
                            .setParticipantId(participantId)
                            .setSalt(saltEncryptor.encrypt(newSalt)));
                    return newSalt;
                });
    }

    private String decryptSalt(SaltToken saltToken) {
        try {
            return saltEncryptor.decrypt(saltToken.getSalt());
        } catch (Exception e) {
            log.error("Failed to decrypt salt for participant {} in study {}", saltToken.getParticipantId(), saltToken.getStudyId(), e);
            throw new RuntimeException("Failed to decrypt salt: " + e.getMessage(), e);
        }
    }

    private String hashToken(String input) {
        if (input == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(properties.getHashAlgorithm());
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Hash algorithm {} not found", properties.getHashAlgorithm());
            return input;
        }
    }

    private String generateCode() {
        String chars = "";
        if (properties.isUseLetters()) {
            chars += "ABCDEFGHKLMPRSTUVWXYZ";
        }
        if (properties.isUseNumbers()) {
            chars += "23456789";
        }
        if (chars.isEmpty()) {
            chars = "ABCDEFGHKLMPRSTUVWXYZ23456789";
        }
        return RandomStringUtils.random(properties.getLength(), 0, 0,
                false, false, chars.toCharArray());
    }

    private LoginToken decryptToken(LoginToken token, Long studyId, Integer participantId) {
        String salt = getOrCreateSalt(studyId, participantId);
        TextEncryptor tokenEncryptor = Encryptors.text(properties.getEncryptionKey(), hashToken(salt));
        return token.setCode(tokenEncryptor.decrypt(token.getCode()));
    }
}
