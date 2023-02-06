package io.redlink.more.studymanager.model;

import java.time.Instant;

public record ParticipationData(
    Integer observationId,
    Integer participantId,
    Integer studyGroupId,
    boolean dataReceived,
    Instant lastDataReceived
){}