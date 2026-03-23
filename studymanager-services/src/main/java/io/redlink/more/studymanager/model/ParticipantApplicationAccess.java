/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model;

import java.util.Objects;

public class ParticipantApplicationAccess {
    private boolean newlyCreated;
    private String accessCode;
    private String applicationType;
    private String applicationUrl;

    public ParticipantApplicationAccess() {
    }

    public boolean isNewlyCreated() {
        return newlyCreated;
    }

    public ParticipantApplicationAccess setNewlyCreated(boolean newlyCreated) {
        this.newlyCreated = newlyCreated;
        return this;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public ParticipantApplicationAccess setAccessCode(String accessCode) {
        this.accessCode = accessCode;
        return this;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public ParticipantApplicationAccess setApplicationType(String applicationType) {
        this.applicationType = applicationType;
        return this;
    }

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public ParticipantApplicationAccess setApplicationUrl(String applicationUrl) {
        this.applicationUrl = applicationUrl;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParticipantApplicationAccess that = (ParticipantApplicationAccess) o;
        return Objects.equals(accessCode, that.accessCode) && Objects.equals(applicationType, that.applicationType) && Objects.equals(applicationUrl, that.applicationUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessCode, applicationType, applicationUrl);
    }

    @Override
    public String toString() {
        return "ParticipantApplicationAccess{" +
                "accessCode='" + accessCode + '\'' +
                ", applicationType='" + applicationType + '\'' +
                ", applicationUrl='" + applicationUrl + '\'' +
                '}';
    }
}
