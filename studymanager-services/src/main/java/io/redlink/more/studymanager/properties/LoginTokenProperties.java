/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
@ConfigurationProperties(prefix = "more.login-token")
public class LoginTokenProperties {

    private boolean useNumbers = true;
    private boolean useLetters = true;
    private Integer length = null;
    private String encryptionKey;
    private String saltKey;
    private String hashAlgorithm = "SHA-256";

    public boolean isUseNumbers() {
        return useNumbers;
    }

    public void setUseNumbers(boolean useNumbers) {
        this.useNumbers = useNumbers;
    }

    public boolean isUseLetters() {
        return useLetters;
    }

    public void setUseLetters(boolean useLetters) {
        this.useLetters = useLetters;
    }

    public int getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public String getSaltKey() {
        return saltKey;
    }

    public void setSaltKey(String saltKey) {
        this.saltKey = saltKey;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    @PostConstruct
    public void validateConfiguration() {
        if (getEncryptionKey() == null || getEncryptionKey().isBlank()) {
            throw new IllegalStateException("Login token encryption key must be provided.");
        }
        if (getSaltKey() == null || getSaltKey().isBlank()) {
            throw new IllegalStateException("Login token salt key must be provided.");
        }
        if (length == null || length < 4) {
            throw new IllegalStateException("Login token length must be greater than or equal to 4.");
        }
        try {
            MessageDigest.getInstance(getHashAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Invalid hash algorithm: " + getHashAlgorithm(), e);
        }
    }

}
