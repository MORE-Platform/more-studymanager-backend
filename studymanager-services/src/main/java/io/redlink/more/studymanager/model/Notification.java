/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model;

import java.util.Map;

public class Notification {

    public enum Type {
        DATA, TEXT
    }
    private Long studyId;
    private Integer participantId;
    private String msgId;
    private Type type;
    private Map<String, String> data;

    public Long getStudyId() {
        return studyId;
    }

    public Notification setStudyId(Long studyId) {
        this.studyId = studyId;
        return this;
    }

    public Integer getParticipantId() {
        return participantId;
    }

    public Notification setParticipantId(Integer participantId) {
        this.participantId = participantId;
        return this;
    }

    public String getMsgId() {
        return msgId;
    }

    public Notification setMsgId(String msgId) {
        this.msgId = msgId;
        return this;
    }

    public Type getType() {
        return type;
    }

    public Notification setType(Type type) {
        this.type = type;
        return this;
    }

    public Map<String, String> getData() {
        return data;
    }

    public Notification setData(Map<String, String> data) {
        this.data = data;
        return this;
    }
}
