/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model;

import io.redlink.more.studymanager.api.v1.model.ActionDTO;
import io.redlink.more.studymanager.api.v1.model.AuditlogDataDTO;
import io.redlink.more.studymanager.api.v1.model.StudyRoleDTO;

import java.time.Instant;
import java.util.*;

public class Auditlog {
    private Long studyId;
    private UUID auditlogId;
    private String userId;
    private List<StudyRoleDTO> userRoles;
    private String userName;
    private Instant timestamp;
    private ActionDTO action;
    private AuditlogDataDTO.StateEnum state;
    private Map<String, Object> details;

    // constructor-----
    public Auditlog() {

    }
    public Auditlog(Long studyId, UUID auditlogId, String userId, List<StudyRoleDTO> userRoles, String userName, Instant timestamp, ActionDTO action, AuditlogDataDTO.StateEnum state) {
        this.studyId = studyId;
        this.auditlogId = auditlogId;
        this.userId = userId;
        this.userRoles = userRoles;
        this.userName = userName;
        this.timestamp = timestamp;
        this.action = action;
        this.state = state;
        this.details = new HashMap<>();
    }

    // getter & setter---------
    public Auditlog getAuditlog() {
        Auditlog auditlog = new Auditlog();
        auditlog.setStudyId(studyId);
        auditlog.setAuditlogId(auditlogId);
        auditlog.setUserId(userId);
        auditlog.setUserName(userName);
        auditlog.setTimestamp(timestamp);
        auditlog.setAction(action);
        auditlog.setState(state);
        auditlog.setDetails(details);
        return auditlog;
    }

    public Long getStudyId() {
        return studyId;
    }
    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }

    public UUID getAuditlogId() {
        return auditlogId;
    }
    public void setAuditlogId(UUID auditlogId) {
        this.auditlogId = auditlogId;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<StudyRoleDTO> getUserRoles() {
        return userRoles;
    }
    public void setUserRoles(List<StudyRoleDTO> userRoles) {
        this.userRoles = userRoles;
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public ActionDTO getAction() {
        return action;
    }
    public void setAction(ActionDTO action) {
        this.action = action;
    }

    public AuditlogDataDTO.StateEnum getState() {
        return state;
    }
    public void setState(AuditlogDataDTO.StateEnum state) {
        this.state = state;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
}
