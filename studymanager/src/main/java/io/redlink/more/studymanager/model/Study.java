/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

public class Study {
    private Long studyId;
    private String title;
    private String purpose;
    private String participantInfo;
    private String consentInfo;
    private String finishText;
    private Status studyState;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private Instant created;
    private Instant modified;
    private Contact contact;
    private Set<StudyRole> userRoles;

    public enum Status {
        DRAFT("draft"),
        ACTIVE("active"),
        PAUSED("paused"),
        CLOSED("closed");

        private final String value;

        Status(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public Long getStudyId() {
        return studyId;
    }

    public Study setStudyId(Long studyId) {
        this.studyId = studyId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Study setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getPurpose() {
        return purpose;
    }

    public Study setPurpose(String purpose) {
        this.purpose = purpose;
        return this;
    }

    public String getParticipantInfo() {
        return participantInfo;
    }

    public Study setParticipantInfo(String participantInfo) {
        this.participantInfo = participantInfo;
        return this;
    }

    public String getConsentInfo() {
        return consentInfo;
    }

    public Study setConsentInfo(String consentInfo) {
        this.consentInfo = consentInfo;
        return this;
    }

    public String getFinishText() {
        return finishText;
    }

    public Study setFinishText(String finishText) {
        this.finishText = finishText;
        return this;
    }

    public Status getStudyState() {
        return studyState;
    }

    public Study setStudyState(Status studyState) {
        this.studyState = studyState;
        return this;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public Study setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Study setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    public LocalDate getPlannedStartDate() {
        return plannedStartDate;
    }

    public Study setPlannedStartDate(LocalDate plannedStartDate) {
        this.plannedStartDate = plannedStartDate;
        return this;
    }

    public LocalDate getPlannedEndDate() {
        return plannedEndDate;
    }

    public Study setPlannedEndDate(LocalDate plannedEndDate) {
        this.plannedEndDate = plannedEndDate;
        return this;
    }

    public Instant getCreated() {
        return created;
    }

    public Study setCreated(Instant created) {
        this.created = created;
        return this;
    }

    public Instant getModified() {
        return modified;
    }

    public Study setModified(Instant modified) {
        this.modified = modified;
        return this;
    }

    public Set<StudyRole> getUserRoles() {
        return userRoles;
    }

    public Study setUserRoles(Set<StudyRole> userRoles) {
        this.userRoles = userRoles;
        return this;
    }

    public Contact getContact() { return contact; }

    public Study setContact(Contact contact) {
        this.contact = contact;
        return this;
    }

    @Override
    public String toString() {
        return "Study{" +
                "studyId=" + studyId +
                ", title='" + title + '\'' +
                ", purpose='" + purpose + '\'' +
                ", participantInfo='" + participantInfo + '\'' +
                ", consentInfo='" + consentInfo + '\'' +
                ", studyState=" + studyState +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", plannedStartDate=" + plannedStartDate +
                ", plannedEndDate=" + plannedEndDate +
                ", created=" + created +
                ", modified=" + modified +
                ", institute=" + contact.getInstitute() +
                ", contactPerson=" + contact.getPerson() +
                ", contactEmail=" + contact.getEmail() +
                ", contactPhoneNumber=" + contact.getPhoneNumber() +
                '}';
    }
}
