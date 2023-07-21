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
