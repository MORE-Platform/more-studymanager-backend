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
    private Status studyState;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private Instant created;
    private Instant modified;
    private String institute;
    private String contactPerson;
    private String contactEmail;
    private String contactPhoneNumber;
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

    public String getInstitute() { return institute; }

    public Study setInstitute(String institute) {
        this.institute = institute;
        return this;
    }

    public String getContactPerson() { return contactPerson; }

    public Study setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
        return this;
    }

    public String getContactEmail() { return contactEmail; }

    public Study setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
        return this;
    }

    public String getContactPhoneNumber() { return contactPhoneNumber; }

    public Study setContactPhoneNumber(String contactPhoneNumber) {
        this.contactPhoneNumber = contactPhoneNumber;
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
                ", institute=" + institute +
                ", contactPerson=" + contactPerson +
                ", contactEmail=" + contactEmail +
                ", contactPhoneNumber=" + contactPhoneNumber +
                '}';
    }
}
