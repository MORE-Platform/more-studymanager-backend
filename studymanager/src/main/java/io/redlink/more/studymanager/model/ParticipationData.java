package io.redlink.more.studymanager.model;

import java.time.Instant;
import java.util.Comparator;

public record ParticipationData(
        Integer observationId,
        Integer participantId,
        Integer studyGroupId,
        boolean dataReceived,
        Instant lastDataReceived
) implements Comparable<ParticipationData> {

    public static final Comparator<ParticipationData> PARTICIPATION_DATA_COMPARATOR =
            Comparator.comparing(ParticipationData::observationId)
                    .thenComparing(ParticipationData::studyGroupId)
                    .thenComparing(ParticipationData::participantId);

    @Override
    public int compareTo(ParticipationData compParticipation) {
        return PARTICIPATION_DATA_COMPARATOR.compare(this, compParticipation);
    }
}