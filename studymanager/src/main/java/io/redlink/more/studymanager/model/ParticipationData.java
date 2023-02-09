package io.redlink.more.studymanager.model;

import java.time.Instant;
import java.util.Comparator;

public record ParticipationData(
        Pair observationData,
        Pair participantData,
        Pair studyGroupData,
        boolean dataReceived,
        Instant lastDataReceived
) implements Comparable<ParticipationData> {

    public static final Comparator<ParticipationData> PARTICIPATION_DATA_COMPARATOR =

            Comparator.comparing(ParticipationData::observationData)
                    .thenComparing(ParticipationData::studyGroupData)
                    .thenComparing(ParticipationData::participantData);

    @Override
    public int compareTo(ParticipationData compParticipation) {
        return PARTICIPATION_DATA_COMPARATOR.compare(this, compParticipation);
    }

    public record Pair(
            int id,
            String title
    ) implements Comparable<Pair>{

        public static final Comparator<Pair> PAIR_COMPARATOR =
                Comparator.comparing(Pair::id);
        @Override
        public int compareTo(Pair compData) {
            return PAIR_COMPARATOR.compare(this, compData);
        }
    }
}