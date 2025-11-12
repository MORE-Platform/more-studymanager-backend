/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model.data;

import java.util.Map;

public class SimpleDataPoint {
    private int participantId;
    private int observationId;
    private String time;
    private Map<String, Object> data;

    public int getParticipantId() {
        return participantId;
    }

    public SimpleDataPoint setParticipantId(int participantId) {
        this.participantId = participantId;
        return this;
    }

    public int getObservationId() {
        return observationId;
    }

    public SimpleDataPoint setObservationId(int observationId) {
        this.observationId = observationId;
        return this;
    }

    public String getTime() {
        return time;
    }

    public SimpleDataPoint setTime(String time) {
        this.time = time;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public SimpleDataPoint setData(Map<String, Object> data) {
        this.data = data;
        return this;
    }
}
