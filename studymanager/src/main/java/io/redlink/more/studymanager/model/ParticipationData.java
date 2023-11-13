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
import java.util.Comparator;

public record ParticipationData(
        NamedId observationNamedId,
        String observationType,
        NamedId participantNamedId,
        NamedId studyGroupNamedId,
        boolean dataReceived,
        Instant lastDataReceived
) implements Comparable<ParticipationData> {

    public static final Comparator<ParticipationData> PARTICIPATION_DATA_COMPARATOR =

            Comparator.comparing(ParticipationData::observationNamedId)
                    .thenComparing(ParticipationData::studyGroupNamedId, Comparator.nullsFirst(Comparator.naturalOrder()))
                    .thenComparing(ParticipationData::participantNamedId);

    @Override
    public int compareTo(ParticipationData compParticipation) {
        return PARTICIPATION_DATA_COMPARATOR.compare(this, compParticipation);
    }

    public record NamedId(
            int id,
            String title
    ) implements Comparable<NamedId>{

        public static final Comparator<NamedId> PAIR_COMPARATOR =
                Comparator.comparing(NamedId::id);
        @Override
        public int compareTo(NamedId compData) {
            return PAIR_COMPARATOR.compare(this, compData);
        }
    }
}