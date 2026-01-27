package io.redlink.more.studymanager.model;

import java.util.Map;

public record ParticipantWithObservationProperties(
        Integer participantId,
        Long studyId,
        Integer observationId,
        Map<String, Object> properties
) {
}
